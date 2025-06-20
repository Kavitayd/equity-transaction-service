//package com.equity.transaction.service.repository;
//
//import com.equity.transaction.service.model.TransactionDTO;
//import org.springframework.data.mongodb.repository.MongoRepository;
//
//public interface TransactionRepository extends MongoRepository<TransactionDTO, String> {
//}

package com.equity.transaction.service.repository;

import com.equity.transaction.service.model.TransactionDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface TransactionRepository extends MongoRepository<TransactionDTO, String> {

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
}

