package com.gxufl.coffee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxufl.coffee.common.BizException;
import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.PageUtil;
import com.gxufl.coffee.dto.CatalogDtos;
import com.gxufl.coffee.entity.Product;
import com.gxufl.coffee.entity.Review;
import com.gxufl.coffee.entity.User;
import com.gxufl.coffee.mapper.ProductMapper;
import com.gxufl.coffee.mapper.ReviewMapper;
import com.gxufl.coffee.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Transactional
    public CatalogDtos.ReviewVO create(Long userId, CatalogDtos.ReviewCreate request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BizException("商品不存在");
        }
        User user = userMapper.selectById(userId);
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(product.getId());
        review.setNickname(user == null ? "咖啡爱好者" : user.getNickname());
        review.setRating(request.getRating());
        review.setContent(request.getContent().trim());
        review.setStatus(1);
        reviewMapper.insert(review);
        refreshProductRating(product.getId());
        return toVOs(Collections.singletonList(review)).get(0);
    }

    public PageResult<CatalogDtos.ReviewVO> listByProduct(Long productId, Integer page, Integer pageSize) {
        Page<Review> result = reviewMapper.selectPage(PageUtil.of(page, pageSize),
                Wrappers.<Review>lambdaQuery()
                        .eq(Review::getProductId, productId)
                        .eq(Review::getStatus, 1)
                        .orderByDesc(Review::getId));
        return PageResult.of(result, toVOs(result.getRecords()));
    }

    public PageResult<CatalogDtos.ReviewVO> mine(Long userId, Integer page, Integer pageSize) {
        Page<Review> result = reviewMapper.selectPage(PageUtil.of(page, pageSize),
                Wrappers.<Review>lambdaQuery()
                        .eq(Review::getUserId, userId)
                        .orderByDesc(Review::getId));
        return PageResult.of(result, toVOs(result.getRecords()));
    }

    public PageResult<CatalogDtos.ReviewVO> adminList(String keyword, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Review> wrapper = Wrappers.<Review>lambdaQuery()
                .eq(status != null, Review::getStatus, status)
                .orderByDesc(Review::getId);
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Review::getContent, kw).or().like(Review::getNickname, kw));
        }
        Page<Review> result = reviewMapper.selectPage(PageUtil.of(page, pageSize), wrapper);
        return PageResult.of(result, toVOs(result.getRecords()));
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BizException("评价不存在");
        }
        review.setStatus(status == 1 ? 1 : 0);
        reviewMapper.updateById(review);
        refreshProductRating(review.getProductId());
    }

    @Transactional
    public void delete(Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return;
        }
        reviewMapper.deleteById(id);
        refreshProductRating(review.getProductId());
    }

    private void refreshProductRating(Long productId) {
        List<Review> reviews = reviewMapper.selectList(Wrappers.<Review>lambdaQuery()
                .eq(Review::getProductId, productId)
                .eq(Review::getStatus, 1));
        BigDecimal rating = new BigDecimal("5.0");
        if (!reviews.isEmpty()) {
            int sum = reviews.stream().mapToInt(Review::getRating).sum();
            rating = BigDecimal.valueOf(sum)
                    .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
        }
        Product product = new Product();
        product.setId(productId);
        product.setRating(rating);
        productMapper.updateById(product);
    }

    private List<CatalogDtos.ReviewVO> toVOs(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Product> products = productMapper.selectBatchIds(
                        reviews.stream().map(Review::getProductId).distinct().toList())
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        List<CatalogDtos.ReviewVO> result = new ArrayList<>();
        for (Review review : reviews) {
            Product product = products.get(review.getProductId());
            CatalogDtos.ReviewVO vo = new CatalogDtos.ReviewVO();
            vo.setId(review.getId());
            vo.setProductId(review.getProductId());
            vo.setProductName(product == null ? "已下架商品" : product.getName());
            vo.setProductImage(product == null ? null : product.getImage());
            vo.setNickname(review.getNickname());
            vo.setRating(review.getRating());
            vo.setContent(review.getContent());
            vo.setStatus(review.getStatus());
            vo.setCreatedAt(review.getCreatedAt());
            result.add(vo);
        }
        return result;
    }
}
