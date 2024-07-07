package com.qthuy2k1.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class PaymentModel {
    @Id
    private String ID;
    private Long Amount;
    private String BankCode;
    private String BankTranNo;
    private String CardType;
    private LocalDateTime PayDate;
    private String OrderInfo;
    private BigDecimal TransactionNo;
    private Integer TransactionStatus;
    private Integer OrderID;
}
