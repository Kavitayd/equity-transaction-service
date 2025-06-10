package com.equity.transaction.service.service;

import com.equity.transaction.service.model.TransactionDTO;

import java.util.List;

public interface MongoService {
    void saveTransactions(List<TransactionDTO> transactions);
}

