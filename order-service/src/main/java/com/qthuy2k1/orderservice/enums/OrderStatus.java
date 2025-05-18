package com.qthuy2k1.orderservice.enums;

import com.qthuy2k1.orderservice.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum OrderStatus {
    //    status = 'Pending' THEN 1
    PENDING("Pending"),
    //    status = 'Processing' THEN 2
    PROCESSING("Processing"),
    //    status = 'Shipping' THEN 3
    SHIPPING("Shipping"),
    //    status = 'Delivered' THEN 4
    DELIVERED("Delivered"),
    //    status = 'Canceled' THEN 5
    CANCELED("Canceled");

    private final String label;

    public static OrderStatus fromLabel(String inputLabel) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getLabel().equalsIgnoreCase(inputLabel)) {
                return status;
            }
        }
        throw new AppException(ErrorCode.INVALID_STATUS_LABEL);
    }
}