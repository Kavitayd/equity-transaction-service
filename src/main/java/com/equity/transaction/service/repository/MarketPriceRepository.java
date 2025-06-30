package com.equity.transaction.service.repository;

import com.equity.transaction.service.model.MarketPriceDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MarketPriceRepository extends MongoRepository<MarketPriceDTO, String> {

    @Query(value = "{ 'securityCode': ?0, 'date': ?1 }", fields = "{ 'price' : 1 }")
    Double findLatestPriceBySecurityCodeAndDate(String securityCode, LocalDate date);

    @Query(value = "{ 'securityCode': ?0, 'date': { $lt: ?1 } }", sort = "{ 'date' : -1 }", fields = "{ 'price' : 1 }")
    Double findLatestPriceBySecurityCodeBeforeDate(String securityCode, LocalDate date);

    // We'll add methods soon like: findBySecurityCodeAndNearestDate()
}

