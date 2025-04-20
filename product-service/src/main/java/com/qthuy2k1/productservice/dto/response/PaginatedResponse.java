package com.qthuy2k1.productservice.dto.response;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> data;
    private Pagination pagination;

    // Constructors, Getters, Setters

    public static class Pagination {
        private long totalRecords;
        private int currentPage;
        private int totalPages;
        private Integer nextPage;
        private Integer prevPage;

        // Constructors, Getters, Setters
    }
}
