package com.equity.transaction.service.model;

import com.equity.transaction.service.service.util.LocalDateConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "TransactionMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class TransactionDTO {
    @Id
    private String id = UUID.randomUUID().toString();

    @CsvBindByName(column = "Client Code")
    private Long clientCode;
    @CsvBindByName(column = "Client Name")
    private String clientName;
    @CsvBindByName(column = "Client Type")
    private String eventType;
    @CsvCustomBindByName(column = "Trade Date", converter = LocalDateConverter.class)
    @DateTimeFormat(pattern = "dd-MMM-yy")
    private LocalDate tradeDate;
    @CsvCustomBindByName (column = "Settlement Date", converter = LocalDateConverter.class)
    @DateTimeFormat(pattern = "dd-MMM-yy")
    private LocalDate settlementDate;
    @CsvBindByName(column = "Security Code")
    private String securityCode;
    @CsvBindByName(column = "Quantity")
    private Integer quantity;
    @CsvBindByName(column = "Rate")
    private Double rate;
    @CsvBindByName(column = "Stamp Duty")
    private Integer stampDuty;
    @CsvBindByName(column = "STT")
    private Integer sttBrokerage;
    @CsvBindByName(column = "Brokerage")
    private Integer Brokerage;
    @CsvBindByName(column = "Transaction Charges")
    private Integer transactionCharges;
    @CsvBindByName(column = "Turnover Fees")
    private Integer turnoverFees;
    @CsvBindByName(column = "Clearing Charges")
    private Integer clearingCharges;
    @CsvBindByName(column = "GST")
    private Integer GST;
    @CsvBindByName(column = "Amount")
    private Integer Amount;
    @CsvBindByName(column = "Transaction NRD")
    private Integer transactionNrd;
    @CsvBindByName(column = "PRD Flag")
    private Integer prdFlag;
    @CsvBindByName(column = "Gain/Loss")
    private Integer gainLoss;
    @CsvBindByName(column = "Available Sale Quantity")
    private Integer availableSaleQuantity;
    @CsvBindByName(column = "Available Buy Quantity")
    private Integer availableBuyQuantity;
    @CsvBindByName(column = "Transaction ID")
    private Integer transactionId;
}
//public class TransactionDTO {
//    @Id
//    private String id = UUID.randomUUID().toString(); // For MongoDB uniqueness
//    private Long clientCode;
//    private String clientName;
//    private String eventType;
//    private LocalDate tradeDate;
//    private LocalDate settlementDate;
//    private String securityCode;
//    private Integer quantity;
//    private Double rate;
//    private Integer stampDuty;
//    private Integer sttBrokerage;
//    private Integer transactionCharges;
//    private Integer turnoverFees;
//    private Integer clearingCharges;
//    private Integer GST;
//}

