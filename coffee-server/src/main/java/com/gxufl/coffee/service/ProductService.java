package com.gxufl.coffee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxufl.coffee.common.BizException;
import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.PageUtil;
import com.gxufl.coffee.common.TextUtil;
import com.gxufl.coffee.dto.CatalogDtos;
import com.gxufl.coffee.entity.CartItem;
import com.gxufl.coffee.entity.Category;
import com.gxufl.coffee.entity.Product;
import com.gxufl.coffee.entity.ProductSpec;
import com.gxufl.coffee.entity.Review;
import com.gxufl.coffee.mapper.CartItemMapper;
import com.gxufl.coffee.mapper.CategoryMapper;
import com.gxufl.coffee.mapper.ProductMapper;
import com.gxufl.coffee.mapper.ProductSpecMapper;
import com.gxufl.coffee.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductSpecMapper specMapper;
    private final CategoryMapper categoryMapper;
    private final CartItemMapper cartItemMapper;
    private final ReviewMapper reviewMapper;

    public PageResult<CatalogDtos.ProductVO> listPublic(String keyword, Long categoryId, Boolean recommended,
                                                        Integer page, Integer pageSize) {
        return query(keyword, categoryId, recommended, true, page, pageSize);
    }

    public PageResult<CatalogDtos.ProductVO> listAdmin(String keyword, Long categoryId, Integer page, Integer pageSize) {
        return query(keyword, categoryId, null, false, page, pageSize);
    }

    public List<CatalogDtos.ProductVO> recommended() {
        List<Product> products = productMapper.selectList(Wrappers.<Product>lambdaQuery()
                .eq(Product::getStatus, 1)
                .eq(Product::getRecommended, 1)
                .orderByAsc(Product::getSort)
                .orderByDesc(Product::getId)
                .last("LIMIT 6"));
        return toVOs(products, false);
    }

    private PageResult<CatalogDtos.ProductVO> query(String keyword, Long categoryId, Boolean recommended,
                                                    boolean onlyOnSale, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = Wrappers.lambdaQuery();
        if (onlyOnSale) {
            wrapper.eq(Product::getStatus, 1);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (recommended != null) {
            wrapper.eq(Product::getRecommended, recommended ? 1 : 0);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Product::getName, kw)
                    .or().like(Product::getSubtitle, kw)
                    .or().like(Product::getTags, kw)
                    .or().like(Product::getDescription, kw));
        }
        wrapper.orderByDesc(Product::getRecommended)
                .orderByAsc(Product::getSort)
                .orderByDesc(Product::getId);

        Page<Product> result = productMapper.selectPage(PageUtil.of(page, pageSize), wrapper);
        return PageResult.of(result, toVOs(result.getRecords(), true));
    }

    public CatalogDtos.ProductVO detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BizException(404, "商品不存在或已下架");
        }
        return toVOs(Collections.singletonList(product), true).get(0);
    }

    public CatalogDtos.ProductVO adminDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        return toVOs(Collections.singletonList(product), true).get(0);
    }

    public List<CatalogDtos.ProductVO> toVOs(List<Product> products, boolean withSpecs) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, String> categoryNames = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        Map<Long, List<ProductSpec>> specMap = Collections.emptyMap();
        if (withSpecs) {
            List<Long> ids = products.stream().map(Product::getId).toList();
            specMap = specMapper.selectList(Wrappers.<ProductSpec>lambdaQuery()
                            .in(ProductSpec::getProductId, ids)
                            .orderByAsc(ProductSpec::getSort)
                            .orderByAsc(ProductSpec::getId))
                    .stream().collect(Collectors.groupingBy(ProductSpec::getProductId));
        }
        List<CatalogDtos.ProductVO> result = new ArrayList<>();
        for (Product product : products) {
            CatalogDtos.ProductVO vo = new CatalogDtos.ProductVO();
            vo.setId(product.getId());
            vo.setCategoryId(product.getCategoryId());
            vo.setCategoryName(categoryNames.getOrDefault(product.getCategoryId(), ""));
            vo.setName(product.getName());
            vo.setSubtitle(product.getSubtitle());
            vo.setDescription(product.getDescription());
            vo.setPrice(product.getPrice());
            vo.setOriginalPrice(product.getOriginalPrice());
            List<String> images = TextUtil.split(product.getImages());
            if (images.isEmpty() && product.getImage() != null && !product.getImage().isBlank()) {
                images = new ArrayList<>(List.of(product.getImage()));
            }
            vo.setImages(images);
            vo.setImage(images.isEmpty() ? product.getImage() : images.get(0));
            vo.setTags(TextUtil.split(product.getTags()));
            vo.setOrigin(product.getOrigin());
            vo.setProcess(product.getProcess());
            vo.setTemperature(TextUtil.split(product.getTemperature()));
            vo.setRating(product.getRating());
            vo.setSales(product.getSales());
            vo.setStock(product.getStock());
            vo.setStatus(product.getStatus());
            vo.setRecommended(product.getRecommended());
            vo.setSort(product.getSort());
            vo.setCreatedAt(product.getCreatedAt());
            vo.setSpecs(specMap.getOrDefault(product.getId(), Collections.emptyList()));
            result.add(vo);
        }
        return result;
    }

    @Transactional
    public CatalogDtos.ProductVO create(CatalogDtos.ProductRequest request) {
        validateCategory(request.getCategoryId());
        Product product = new Product();
        apply(product, request, false);
        product.setSales(0);
        product.setRating(request.getRating() == null ? new BigDecimal("5.0") : request.getRating());
        productMapper.insert(product);
        saveSpecs(product.getId(), request.getSpecs());
        return adminDetail(product.getId());
    }

    @Transactional
    public CatalogDtos.ProductVO update(Long id, CatalogDtos.ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        validateCategory(request.getCategoryId());
        apply(product, request, true);
        productMapper.updateById(product);
        specMapper.delete(Wrappers.<ProductSpec>lambdaQuery().eq(ProductSpec::getProductId, id));
        saveSpecs(id, request.getSpecs());
        return adminDetail(id);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return;
        }
        specMapper.delete(Wrappers.<ProductSpec>lambdaQuery().eq(ProductSpec::getProductId, id));
        cartItemMapper.delete(Wrappers.<CartItem>lambdaQuery().eq(CartItem::getProductId, id));
        reviewMapper.delete(Wrappers.<Review>lambdaQuery().eq(Review::getProductId, id));
        productMapper.deleteById(id);
    }

    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status == 1 ? 1 : 0);
        if (productMapper.updateById(product) == 0) {
            throw new BizException("商品不存在");
        }
    }

    public void updateRecommended(Long id, Integer recommended) {
        Product product = new Product();
        product.setId(id);
        product.setRecommended(recommended == 1 ? 1 : 0);
        if (productMapper.updateById(product) == 0) {
            throw new BizException("商品不存在");
        }
    }

    private void validateCategory(Long categoryId) {
        if (categoryId == null || categoryMapper.selectById(categoryId) == null) {
            throw new BizException("商品分类不存在");
        }
    }

    private void apply(Product product, CatalogDtos.ProductRequest request, boolean updating) {
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName().trim());
        product.setSubtitle(request.getSubtitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        if (request.getImages() != null) {
            product.setImages(TextUtil.join(request.getImages()));
        }
        if (request.getImage() != null && !request.getImage().isBlank()) {
            product.setImage(request.getImage().trim());
        } else if (request.getImages() != null && !request.getImages().isEmpty()) {
            product.setImage(request.getImages().get(0));
        } else if (!updating) {
            product.setImage("");
        }
        if (request.getTags() != null) {
            product.setTags(TextUtil.join(request.getTags()));
        }
        product.setOrigin(request.getOrigin());
        product.setProcess(request.getProcess());
        if (request.getTemperature() != null) {
            product.setTemperature(TextUtil.join(request.getTemperature()));
        }
        product.setStock(request.getStock() == null ? 99 : request.getStock());
        product.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        product.setRecommended(request.getRecommended() == null ? 0 : request.getRecommended());
        product.setSort(request.getSort() == null ? 0 : request.getSort());
        if (request.getRating() != null) {
            product.setRating(request.getRating());
        }
    }

    private void saveSpecs(Long productId, List<CatalogDtos.SpecRequest> specs) {
        if (specs == null) {
            return;
        }
        int index = 0;
        for (CatalogDtos.SpecRequest specRequest : specs) {
            if (specRequest.getName() == null || specRequest.getName().isBlank()) {
                continue;
            }
            ProductSpec spec = new ProductSpec();
            spec.setProductId(productId);
            spec.setName(specRequest.getName().trim());
            spec.setPriceDelta(specRequest.getPriceDelta() == null ? BigDecimal.ZERO : specRequest.getPriceDelta());
            spec.setSort(specRequest.getSort() == null ? index : specRequest.getSort());
            specMapper.insert(spec);
            index++;
        }
    }
}
