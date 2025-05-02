package com.qthuy2k1.orderservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReportResponse {
    String reportPeriod;
    Long totalOrders;
    String avgOrderValue;
    Long newCustomers;
    Long returningCustomers;
    Long pending;
    Long processing;
    Long shipping;
    Long delivered;
    Long canceled;
    List<ProductResponse> topSellingProducts;
}
