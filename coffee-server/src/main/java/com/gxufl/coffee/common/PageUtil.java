package com.gxufl.coffee.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public final class PageUtil {

    private PageUtil() {
    }

    public static <T> Page<T> of(Integer page, Integer pageSize) {
        long current = page == null || page < 1 ? 1 : page;
        long size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        return new Page<>(current, size);
    }
}
