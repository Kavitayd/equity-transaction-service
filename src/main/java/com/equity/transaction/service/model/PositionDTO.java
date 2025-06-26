package com.equity.transaction.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "PositionMaster")
public class PositionDTO {
    @Id
    private String id = UUID.randomUUID().toString();
    private Long clientCode;
    private LocalDate saleDate;
    private LocalDate date;
    private String securityCode;
    private Integer qty;
    private Double holdingCost;
    private Double averageCostPerUnit;
    private Integer corpActionQty;
    private Double marketPricePerUnitOnToday;
    private Double marketValueOnToday;
    private Double cumulativeUnrealisedGainLossUptoToday;



}
