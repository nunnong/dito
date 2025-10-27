package com.ssafy.Dito.global.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PageResult<T> extends CommonResult {
    private final List<T> data;
    private final PageInfo pageInfo;

    public PageResult(String message, Page<T> pageData) {
        super(false, message);
        this.data = pageData.getContent();
        this.pageInfo = new PageInfo(pageData);
    }

    @Getter
    public static class PageInfo {
        private final long totalElements;
        private final int totalPages;
        private final int     page;
        private final int     size;
        private final boolean hasPrevious;
        private final boolean hasNext;

        public PageInfo(Page<?> page) {
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.page = page.getNumber();
            this.size = page.getSize();
            this.hasPrevious = page.hasPrevious();
            this.hasNext = page.hasNext();
        }
    }
}

