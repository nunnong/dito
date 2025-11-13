package com.ssafy.Dito.global.util;

import com.ssafy.Dito.global.exception.PageNotFoundException;
import java.util.List;
import java.util.function.LongSupplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

public class PageUtils {

    @Deprecated
    public static <T> Page<T> of(List<T> content, Pageable pageable, LongSupplier totalSupplier) {
        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    try {
                        return totalSupplier.getAsLong();
                    } catch (NullPointerException e) {
                        throw new PageNotFoundException();
                    }
                }
        );
    }

    public static <T> Page<T> of(List<T> content, Pageable pageable, Long count) {
        if (count == null) {
            count = 0L;
        }

        if (count == 0L && pageable.getPageNumber() != 0) {
            throw new PageNotFoundException();
        }

        return new PageImpl<>(
                content,
                pageable,
                count
        );
    }

}
