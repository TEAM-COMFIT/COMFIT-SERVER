package sopt.comfit.global.dto;


import org.springframework.data.domain.Page;

import java.util.List;

public record PageDto<T>(
        List<T> content,

        int currentPage,

        int totalPage,

        long totalElements

) {
    public static <T> PageDto<T> from(Page<T> page) {
        return new PageDto<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}