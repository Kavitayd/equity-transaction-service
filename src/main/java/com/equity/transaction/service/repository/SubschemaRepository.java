package com.equity.transaction.service.repository;

import com.equity.transaction.service.model.CapitalGainBreakupDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface SubschemaRepository extends MongoRepository<CapitalGainBreakupDTO, String> {
    // Custom query methods will go here
    @Query("{ 'clientId': ?0, 'securityCode': ?1, 'saleDate': { $lte: ?2 } }")
    Integer sumQuantityByClientIdAndSecurityCodeAndSaleDateLessThanEqual(
            Long clientId, String securityCode, Date saleDate
    );

    @Query("{ 'clientId': ?0, 'securityCode': ?1, 'saleDate': { $lte: ?2 } }")
    Double sumPurchaseValueByClientIdAndSecurityCodeAndSaleDateLessThanEqual(
            Long clientId, String securityCode, Date saleDate
    );



}
