package com.equity.transaction.service.service.impl;

import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.repository.MarketPriceRepository;
import com.equity.transaction.service.repository.PositionRepository;
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
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private MarketPriceRepository marketPriceRepository;

    @Override
    public void saveTransactions(List<TransactionDTO> transactions) {
        transactionRepository.saveAll(transactions);
        System.out.println("Saved " + transactions.size() + " transactions to MongoDB");

    }
    @Override
    public void deleteAllTransactions() {
        transactionRepository.deleteAll();
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public void savePositions(List<PositionDTO> positions) {
        positionRepository.saveAll(positions);
    }

    @Override
    public void deleteAllPositions() {
        positionRepository.deleteAll();
    }

    @Override
    public void saveMarketPrices(List<MarketPriceDTO> marketPrices) {
        marketPriceRepository.saveAll(marketPrices);
    }

    @Override
    public void deleteAllMarketPrices() {
        marketPriceRepository.deleteAll();
    }

}