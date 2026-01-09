package sopt.comfit.global.dto;


import org.springframework.data.domain.Page;

import java.util.List;

public record PageDto<T>(
        List<T> content,

        int currentPage,

        int currentSize,

        int totalPage,

        long totalElements,

        boolean hasPrevious,

        boolean hasNext
) {
    public static <T> PageDto<T> from(Page<T> page) {
        return new PageDto<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getNumberOfElements(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasPrevious(),
                page.hasNext()
        );
    }
}