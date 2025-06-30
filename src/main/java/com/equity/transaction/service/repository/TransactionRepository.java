package com.equity.transaction.service.repository;

import com.equity.transaction.service.model.TransactionDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<TransactionDTO, String> {

    @Query(value = "{}", sort = "{ settlementDate : -1 }")
    TransactionDTO findTopByOrderBySettlementDateDesc();

    @Query("{ 'eventType': ?0, 'clientCode': ?1, 'securityCode': ?2, 'settlementDate': { $lte: ?3 } }")
    Integer sumQuantityByEventTypeAndClientCodeAndSecurityCodeAndSettlementDateLessThanEqual(
            String eventType, Long clientCode, String securityCode, Date settlementDate
    );

    @Query("{ 'eventType': ?0, 'clientCode': ?1, 'securityCode': ?2, 'settlementDate': { $lte: ?3 } }")
    Double sumAmountByEventTypeAndClientCodeAndSecurityCodeAndSettlementDateLessThanEqual(
            String eventType, Long clientCode, String securityCode, Date settlementDate
    );

    @Query("{'eventType:?0, 'clientCode': ?1, 'securtyCode': ?2,''settlementDaate':{$1te: ?3}}")
    // Fetch all sale transactions (EQ_SAL)
    List<TransactionDTO> findByEventType(String eventType);

    // Fetch purchase transactions (EQ_PUR) for a sale match (FIFO logic)
    List<TransactionDTO> findByEventTypeAndClientCodeAndSecurityCodeAndSettlementDateLessThanEqualAndAvailableBuyQuantityGreaterThan(
            String eventType,
            int clientCode,
            String securityCode,
            Date settlementDate,
            int availableBuyQuantityThreshold
    );

    // ✅ New method: Get all transactions with EQ_PUR and settlementDate <= given date
    @Query(value = "{ 'eventType': 'EQ_PUR', 'settlementDate': { $lte: ?0 } }", fields = "{ 'clientCode': 1, 'securityCode': 1 }")
    List<TransactionDTO> findClientSecurityPairsForDate(Date date);
}
