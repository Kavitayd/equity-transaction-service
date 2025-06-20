package com.equity.transaction.service.service;

import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.model.TransactionDTO;

import java.util.List;

public interface MongoService {
    void saveTransactions(List<TransactionDTO> transactions);
    void deleteAllTransactions();

    List<TransactionDTO> getAllTransactions();

    void savePositions(List<PositionDTO> positions);
    void deleteAllPositions();

    void saveMarketPrices(List<MarketPriceDTO> marketPrices);
    void deleteAllMarketPrices();
}

