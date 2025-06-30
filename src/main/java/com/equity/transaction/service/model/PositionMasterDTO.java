package com.equity.transaction.service.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PositionMasterDTO {
    private String clientCode;
    private String securityCode;
    private LocalDate date;
    private int quantity;
    private BigDecimal holdingCost;
    private BigDecimal averageCost;
    private BigDecimal marketPriceT;
    private BigDecimal marketValue;
    private BigDecimal cumulativeUnrealised;
    private BigDecimal marketPriceTMinus1;
    private BigDecimal dailyUnrealised;

}
