package com.qthuy2k1.orderservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReportModel {
    Integer period;
    Long totalorders;
    BigDecimal avgordervalue;
    Long newcustomers;
    Long returningcustomers;
    Long pending;
    Long processing;
    Long shipped;
    Long delivered;
    Long canceled;
//    List<ProductResponse> topSellingProducts;
}
