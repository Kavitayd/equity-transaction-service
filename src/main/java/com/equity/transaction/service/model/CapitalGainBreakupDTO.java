package com.equity.transaction.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "CapitalGainBreakupDTO")
public class CapitalGainBreakupDTO {

    private Long clientCode;
    private String securityCode;

    private Date purchaseDate;
    private Date saleDate;
    private int holdingPeriod;

    private Double purchasePrice;
    private Double salePrice;

    private Double purchaseValue;
    private Double saleValue;
    private Double gainOrLoss;

    private String purchaseTransactionId;
    private String saleTransactionId;

    private Integer balancePurchaseQty;
    private Integer balanceSaleQty;

}
