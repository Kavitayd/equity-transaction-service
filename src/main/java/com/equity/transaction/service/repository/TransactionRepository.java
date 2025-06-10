package com.equity.transaction.service.repository;

import com.equity.transaction.service.model.TransactionDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<TransactionDTO, String> {
}
