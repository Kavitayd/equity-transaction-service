package com.equity.transaction.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "TransactionMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    @Id
    private String id = UUID.randomUUID().toString(); // For MongoDB uniqueness
    private Long clientCode;
    private String clientName;
    private String eventType;
    private LocalDate tradeDate;
    private LocalDate settlementDate;
    private String securityCode;
    private Integer quantity;
    private Integer rate;
    private Integer stampDuty;
    private Integer stt;
    private Integer brokerage;
    private Integer transactionCharges;
    private Integer turnoverFees;
    private Integer clearingCharges;
    private Integer GST;
    private Integer amount;
    private String transactionNrd;
    private String PrdFlag;
    private String gainLoss;
    private Integer availableSaleQuantity;
    private Integer availableBuyQuantity;
    private String transactionId;

}