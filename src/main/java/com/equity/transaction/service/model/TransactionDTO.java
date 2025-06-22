package com.equity.transaction.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;
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
    private Date tradeDate;
    private Date settlementDate;
    private String securityCode;
    private Integer stampDuty;
    private Integer stt;
    private Integer brokerage;
    private Integer transactionCharges;
    private Integer turnoverFees;
    private Integer clearingCharges;
    private Integer GST;
    private String transactionNrd;
    private String PrdFlag;
    private String gainLoss;
    private String transactionId;
    private Double availableBuyQuantity;
    private Double availableSaleQuantity;
    private Double quantity;
    private Double rate;
    private Double amount;
//    private Double  gainOrLoss;
}