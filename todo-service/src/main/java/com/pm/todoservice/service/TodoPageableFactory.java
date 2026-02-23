package com.pm.todoservice.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class TodoPageableFactory {

    public Pageable buildPageable(int page, int size, String sort) {
        if (page < 0) {
            throw new RuntimeException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new RuntimeException("Size must be between 1 and 100");
        }

        return PageRequest.of(page, size, buildSort(sort));
    }

    public Sort buildSort(String sort) {
        String[] sortParts = sort.split(",");
        String field = sortParts[0].trim();
        String direction = sortParts.length > 1 ? sortParts[1].trim() : "asc";

        if (!isAllowedSortField(field)) {
            throw new RuntimeException("Unsupported sort field: " + field);
        }

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(sortDirection, field);
    }

    private boolean isAllowedSortField(String field) {
        return "createdAt".equals(field)
                || "updatedAt".equals(field)
                || "dueDate".equals(field)
                || "priority".equals(field)
                || "title".equals(field)
                || "completed".equals(field);
    }
}
