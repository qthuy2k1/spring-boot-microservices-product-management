package com.qthuy2k1.productservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private Pagination pagination;

    // Constructors, Getters, Setters

    @Data
    public static class Pagination {
        private long totalRecords;
        private int currentPage;
        private int totalPages;
        private Integer nextPage;
        private Integer prevPage;
    }
}
