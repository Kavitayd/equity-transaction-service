package com.equity.transaction.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "CapitalGainBreakupDTO")
public class CapitalGainBreakupDTO {

    private Long clientCode;
    private String securityCode;
    private Date purchaseDate;
    private Date saleDate;
    private Double purchaseRate;
    private Double saleRate;
    private Integer holdingPeriodDays;
    private String capitalGainType;
    private Double quantity;
    private int holdingPeriod;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double purchasePrice;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double salePrice;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double purchaseValue;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double saleValue;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double gainOrLoss;
    private String purchaseTransactionId;
    private String saleTransactionId;
    private Double  balancePurchaseQty;
    private Double balanceSaleQty;

}
