package com.qthuy2k1.orderservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductReportList {
    Integer product_id;
    Long product_count;
}
