package com.possaas.possaasbackend.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    private PaginationUtils() {
    }

    public static Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        int validPage = Math.max(page, 0);
        int validSize = size > 0 ? Math.min(size, 100) : Integer.parseInt(DEFAULT_PAGE_SIZE);
        String validSortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;

        Sort.Direction direction = (sortDirection != null && sortDirection.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(validPage, validSize, Sort.by(direction, validSortBy));
    }
}
