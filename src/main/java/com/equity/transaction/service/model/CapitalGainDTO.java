package com.equity.transaction.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "CapitalGainDTO")
public class CapitalGainDTO {
    private Long clientCode;
    private String securityCode;
    private Date saleDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double totalCapitalGain;
    private String prdHoldingFlag;
    private String clientName;
    private String securityName;
    private String isin;
    private String listingStatus;
    private String capitalGainType;
    private String transactionId;
    private List<CapitalGainBreakupDTO> breakup;

}
