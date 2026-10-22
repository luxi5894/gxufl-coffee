package com.gxufl.coffee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gxufl.coffee.common.BizException;
import com.gxufl.coffee.dto.CartDtos;
import com.gxufl.coffee.entity.CartItem;
import com.gxufl.coffee.entity.Product;
import com.gxufl.coffee.entity.ProductSpec;
import com.gxufl.coffee.mapper.CartItemMapper;
import com.gxufl.coffee.mapper.ProductMapper;
import com.gxufl.coffee.mapper.ProductSpecMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper specMapper;

    public List<CartDtos.CartVO> list(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getId));
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Product> products = productMapper.selectBatchIds(
                        items.stream().map(CartItem::getProductId).distinct().toList())
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Long> specIds = items.stream().map(CartItem::getSpecId).filter(id -> id != null).distinct().toList();
        Map<Long, ProductSpec> specs = specIds.isEmpty() ? Collections.emptyMap()
                : specMapper.selectBatchIds(specIds).stream()
                .collect(Collectors.toMap(ProductSpec::getId, Function.identity()));

        List<CartDtos.CartVO> result = new ArrayList<>();
        for (CartItem item : items) {
            Product product = products.get(item.getProductId());
            if (product == null) {
                continue;
            }
            ProductSpec spec = item.getSpecId() == null ? null : specs.get(item.getSpecId());
            CartDtos.CartVO vo = new CartDtos.CartVO();
            vo.setId(item.getId());
            vo.setProductId(product.getId());
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImage());
            vo.setSpecId(item.getSpecId());
            vo.setSpecName(item.getSpecName());
            vo.setQuantity(item.getQuantity());
            vo.setStock(product.getStock());
            vo.setProductStatus(product.getStatus());
            vo.setPrice(unitPrice(product, spec));
            vo.setCreatedAt(item.getCreatedAt());
            result.add(vo);
        }
        return result;
    }

    @Transactional
    public void add(Long userId, CartDtos.Add request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        ProductSpec spec = null;
        if (request.getSpecId() != null) {
            spec = specMapper.selectById(request.getSpecId());
            if (spec == null || !spec.getProductId().equals(product.getId())) {
                throw new BizException("规格不存在");
            }
        }
        if (product.getStock() != null && product.getStock() <= 0) {
            throw new BizException("该商品暂时售罄");
        }

        CartItem existing = cartItemMapper.selectOne(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, request.getProductId())
                .eq(request.getSpecId() != null, CartItem::getSpecId, request.getSpecId())
                .isNull(request.getSpecId() == null, CartItem::getSpecId)
                .last("LIMIT 1"));
        if (existing != null) {
            int quantity = Math.min(99, existing.getQuantity() + request.getQuantity());
            if (product.getStock() != null && quantity > product.getStock()) {
                throw new BizException("库存不足");
            }
            existing.setQuantity(quantity);
            cartItemMapper.updateById(existing);
            return;
        }
        if (product.getStock() != null && request.getQuantity() > product.getStock()) {
            throw new BizException("库存不足");
        }
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(request.getProductId());
        item.setSpecId(request.getSpecId());
        item.setSpecName(spec == null ? "" : spec.getName());
        item.setQuantity(request.getQuantity());
        cartItemMapper.insert(item);
    }

    public void update(Long userId, Long id, CartDtos.Update request) {
        CartItem item = requireItem(userId, id);
        item.setQuantity(request.getQuantity());
        cartItemMapper.updateById(item);
    }

    public void delete(Long userId, Long id) {
        requireItem(userId, id);
        cartItemMapper.deleteById(id);
    }

    public void clear(Long userId) {
        cartItemMapper.delete(Wrappers.<CartItem>lambdaQuery().eq(CartItem::getUserId, userId));
    }

    public Map<String, Object> count(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, userId));
        int count = items.stream().mapToInt(CartItem::getQuantity).sum();
        return Map.of("count", count);
    }

    private CartItem requireItem(Long userId, Long id) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BizException("购物车记录不存在");
        }
        return item;
    }

    public static BigDecimal unitPrice(Product product, ProductSpec spec) {
        BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        if (spec != null && spec.getPriceDelta() != null) {
            price = price.add(spec.getPriceDelta());
        }
        return price;
    }
}
