package com.equity.transaction.service.service.impl;

import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.repository.TransactionRepository;
import com.equity.transaction.service.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MongoServiceImpl implements MongoService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void saveTransactions(List<TransactionDTO> transactions) {
        transactionRepository.saveAll(transactions);
        System.out.println("Saved " + transactions.size() + " transactions to MongoDB");

    }
}