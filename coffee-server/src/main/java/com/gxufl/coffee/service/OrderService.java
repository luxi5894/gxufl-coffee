package com.gxufl.coffee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxufl.coffee.common.BizException;
import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.PageUtil;
import com.gxufl.coffee.dto.OrderDtos;
import com.gxufl.coffee.dto.StatsDtos;
import com.gxufl.coffee.entity.CartItem;
import com.gxufl.coffee.entity.Order;
import com.gxufl.coffee.entity.OrderItem;
import com.gxufl.coffee.entity.Product;
import com.gxufl.coffee.entity.ProductSpec;
import com.gxufl.coffee.entity.User;
import com.gxufl.coffee.mapper.CartItemMapper;
import com.gxufl.coffee.mapper.OrderItemMapper;
import com.gxufl.coffee.mapper.OrderMapper;
import com.gxufl.coffee.mapper.ProductMapper;
import com.gxufl.coffee.mapper.ProductSpecMapper;
import com.gxufl.coffee.mapper.StatsMapper;
import com.gxufl.coffee.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_MAKING = 1;
    public static final int STATUS_READY = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_CANCELLED = 4;

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper specMapper;
    private final UserMapper userMapper;
    private final StatsMapper statsMapper;

    @Transactional
    public OrderDtos.OrderVO create(Long userId, OrderDtos.Create request) {
        List<Line> lines = new ArrayList<>();
        List<Long> cartIds = new ArrayList<>();

        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            List<CartItem> cartItems = cartItemMapper.selectList(Wrappers.<CartItem>lambdaQuery()
                    .eq(CartItem::getUserId, userId)
                    .in(CartItem::getId, request.getCartItemIds()));
            if (cartItems.size() != request.getCartItemIds().size()) {
                throw new BizException("部分购物车商品已失效，请刷新后重试");
            }
            for (CartItem item : cartItems) {
                lines.add(new Line(item.getProductId(), item.getSpecId(), item.getQuantity()));
                cartIds.add(item.getId());
            }
        } else if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderDtos.Item item : request.getItems()) {
                lines.add(new Line(item.getProductId(), item.getSpecId(), item.getQuantity()));
            }
        } else {
            throw new BizException("请选择要结算的商品");
        }

        Map<String, Line> merged = new LinkedHashMap<>();
        for (Line line : lines) {
            String key = line.getProductId() + ":" + line.getSpecId();
            merged.merge(key, line, (a, b) -> new Line(a.getProductId(), a.getSpecId(), a.getQuantity() + b.getQuantity()));
        }

        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Line line : merged.values()) {
            Product product = productMapper.selectById(line.getProductId());
            if (product == null || product.getStatus() == null || product.getStatus() != 1) {
                throw new BizException("商品已下架，请重新选择");
            }
            ProductSpec spec = null;
            if (line.getSpecId() != null) {
                spec = specMapper.selectById(line.getSpecId());
                if (spec == null || !spec.getProductId().equals(product.getId())) {
                    throw new BizException("商品规格已失效，请重新选择");
                }
            }
            int quantity = line.getQuantity();
            int affected = productMapper.update(null, Wrappers.<Product>lambdaUpdate()
                    .eq(Product::getId, product.getId())
                    .ge(Product::getStock, quantity)
                    .setSql("stock = stock - " + quantity)
                    .setSql("sales = sales + " + quantity));
            if (affected == 0) {
                throw new BizException("「" + product.getName() + "」库存不足");
            }
            BigDecimal unitPrice = CartService.unitPrice(product, spec);
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            itemCount += quantity;

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setSpecName(spec == null ? "" : spec.getName());
            orderItem.setPrice(unitPrice);
            orderItem.setQuantity(quantity);
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setOrderNo("GX" + LocalDateTime.now().format(ORDER_NO_FORMAT) + (100 + RANDOM.nextInt(900)));
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setItemCount(itemCount);
        order.setStatus(STATUS_PENDING);
        order.setRemark(request.getRemark());
        order.setPickupCode(String.valueOf(1000 + RANDOM.nextInt(9000)));
        orderMapper.insert(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }
        if (!cartIds.isEmpty()) {
            cartItemMapper.deleteBatchIds(cartIds);
        }
        return toVO(order, orderItems, userMapper.selectById(userId));
    }

    public PageResult<OrderDtos.OrderVO> list(Long userId, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getId);
        Page<Order> result = orderMapper.selectPage(PageUtil.of(page, pageSize), wrapper);
        return PageResult.of(result, toVOs(result.getRecords(), false));
    }

    public OrderDtos.OrderVO detail(Long userId, Long id, boolean admin) {
        Order order = orderMapper.selectById(id);
        if (order == null || (!admin && !order.getUserId().equals(userId))) {
            throw new BizException(404, "订单不存在");
        }
        List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, id)
                .orderByAsc(OrderItem::getId));
        return toVO(order, items, userMapper.selectById(order.getUserId()));
    }

    @Transactional
    public void cancel(Long userId, Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(404, "订单不存在");
        }
        if (order.getStatus() != STATUS_PENDING) {
            throw new BizException("当前订单状态不可取消");
        }
        restoreStock(order.getId());
        order.setStatus(STATUS_CANCELLED);
        orderMapper.updateById(order);
    }

    @Transactional
    public void confirm(Long userId, Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(404, "订单不存在");
        }
        if (order.getStatus() != STATUS_READY) {
            throw new BizException("订单还未制作完成");
        }
        order.setStatus(STATUS_COMPLETED);
        orderMapper.updateById(order);
    }

    public PageResult<OrderDtos.OrderVO> adminList(Integer status, String keyword, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery()
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getId);
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            List<Long> userIds = userMapper.selectList(Wrappers.<User>lambdaQuery()
                            .like(User::getNickname, kw).or().like(User::getPhone, kw))
                    .stream().map(User::getId).toList();
            wrapper.and(w -> {
                w.like(Order::getOrderNo, kw);
                if (!userIds.isEmpty()) {
                    w.or().in(Order::getUserId, userIds);
                }
            });
        }
        Page<Order> result = orderMapper.selectPage(PageUtil.of(page, pageSize), wrapper);
        return PageResult.of(result, toVOs(result.getRecords(), true));
    }

    @Transactional
    public void adminUpdateStatus(Long id, Integer status) {
        if (status == null || status < 0 || status > 4) {
            throw new BizException("订单状态不合法");
        }
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() == STATUS_CANCELLED && status != STATUS_CANCELLED) {
            throw new BizException("已取消的订单不能重新流转");
        }
        if (status == STATUS_CANCELLED && order.getStatus() != STATUS_CANCELLED) {
            restoreStock(order.getId());
        }
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    public StatsDtos.StatsVO stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();

        StatsDtos.StatsVO vo = new StatsDtos.StatsVO();
        vo.setTodayOrders(statsMapper.countOrders(todayStart));
        vo.setTodayRevenue(statsMapper.revenueSince(todayStart));
        vo.setTodayUsers(statsMapper.countNewUsers(todayStart));
        vo.setTotalUsers(userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getRole, "USER")));
        vo.setTotalOrders(orderMapper.selectCount(null));
        vo.setTotalRevenue(statsMapper.revenueSince(LocalDateTime.of(2000, 1, 1, 0, 0)));
        vo.setPendingOrders(statsMapper.countPending());
        vo.setProductCount(productMapper.selectCount(null));

        Map<String, StatsDtos.DailyPoint> trend = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            String date = LocalDate.now().minusDays(6 - i).toString();
            trend.put(date, new StatsDtos.DailyPoint(date, 0, BigDecimal.ZERO));
        }
        for (Map<String, Object> row : statsMapper.dailyTrend(weekStart)) {
            String date = String.valueOf(value(row, "day"));
            StatsDtos.DailyPoint point = trend.get(date);
            if (point != null) {
                point.setOrders(toLong(value(row, "orders")));
                point.setRevenue(toDecimal(value(row, "revenue")));
            }
        }
        vo.setTrend(new ArrayList<>(trend.values()));

        List<StatsDtos.TopProduct> top = new ArrayList<>();
        for (Map<String, Object> row : statsMapper.topProducts(weekStart)) {
            top.add(new StatsDtos.TopProduct(String.valueOf(value(row, "name")),
                    toLong(value(row, "quantity")), toDecimal(value(row, "amount"))));
        }
        vo.setTopProducts(top);

        List<StatsDtos.StatusCount> distribution = new ArrayList<>();
        for (int status = 0; status <= 4; status++) {
            distribution.add(new StatsDtos.StatusCount(status, 0));
        }
        for (Map<String, Object> row : statsMapper.statusDistribution()) {
            int status = (int) toLong(value(row, "status"));
            if (status >= 0 && status <= 4) {
                distribution.get(status).setTotal(toLong(value(row, "total")));
            }
        }
        vo.setStatusDistribution(distribution);

        Page<Order> recent = orderMapper.selectPage(new Page<>(1, 6),
                Wrappers.<Order>lambdaQuery().orderByDesc(Order::getId));
        vo.setRecentOrders(toVOs(recent.getRecords(), true));
        return vo;
    }

    public StatsDtos.UserStatsVO userStats(Long userId) {
        List<Order> orders = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId));
        long completed = orders.stream()
                .filter(order -> order.getStatus() == STATUS_COMPLETED)
                .count();
        BigDecimal spent = orders.stream()
                .filter(order -> order.getStatus() != STATUS_CANCELLED)
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new StatsDtos.UserStatsVO(orders.size(), completed, spent);
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            productMapper.update(null, Wrappers.<Product>lambdaUpdate()
                    .eq(Product::getId, item.getProductId())
                    .setSql("stock = stock + " + item.getQuantity())
                    .setSql("sales = GREATEST(sales - " + item.getQuantity() + ", 0)"));
        }
    }

    private List<OrderDtos.OrderVO> toVOs(List<Order> orders, boolean withUser) {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, User> users = Collections.emptyMap();
        if (withUser) {
            users = userMapper.selectBatchIds(orders.stream().map(Order::getUserId).distinct().toList())
                    .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        }
        Map<Long, List<OrderItem>> itemsByOrder = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                        .in(OrderItem::getOrderId, orders.stream().map(Order::getId).toList())
                        .orderByAsc(OrderItem::getId))
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        List<OrderDtos.OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(toVO(order, itemsByOrder.getOrDefault(order.getId(), Collections.emptyList()),
                    withUser ? users.get(order.getUserId()) : null));
        }
        return result;
    }

    private OrderDtos.OrderVO toVO(Order order, List<OrderItem> items, User user) {
        OrderDtos.OrderVO vo = new OrderDtos.OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setItemCount(order.getItemCount());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setPickupCode(order.getPickupCode());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setItems(items);
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserPhone(user.getPhone());
        }
        return vo;
    }

    private static Object value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value : row.get(key.toUpperCase());
    }

    private static long toLong(Object value) {
        return value == null ? 0 : Long.parseLong(value.toString());
    }

    private static BigDecimal toDecimal(Object value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    @Data
    @AllArgsConstructor
    private static class Line {
        private Long productId;
        private Long specId;
        private Integer quantity;
    }
}
