package com.equity.transaction.service.model;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
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
@Document(collection = "MarketPriceMaster")
public class MarketPriceDTO {
    @Id
    private String id = UUID.randomUUID().toString();

    private String securityCode;
    private LocalDate date;
    private Double price;
}
