package com.gxufl.coffee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gxufl.coffee.common.BizException;
import com.gxufl.coffee.dto.CatalogDtos;
import com.gxufl.coffee.entity.Category;
import com.gxufl.coffee.entity.Product;
import com.gxufl.coffee.mapper.CategoryMapper;
import com.gxufl.coffee.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public List<Category> listPublic() {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    public List<Category> listAll() {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    @Transactional
    public Category create(CatalogDtos.CategoryRequest request) {
        Category category = new Category();
        apply(category, request);
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public Category update(Long id, CatalogDtos.CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }
        apply(category, request);
        categoryMapper.updateById(category);
        return category;
    }

    @Transactional
    public void delete(Long id) {
        Long count = productMapper.selectCount(Wrappers.<Product>lambdaQuery().eq(Product::getCategoryId, id));
        if (count > 0) {
            throw new BizException("该分类下还有商品，暂不能删除");
        }
        categoryMapper.deleteById(id);
    }

    private void apply(Category category, CatalogDtos.CategoryRequest request) {
        category.setName(request.getName().trim());
        category.setNameEn(request.getNameEn());
        category.setIcon(request.getIcon());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }
}
