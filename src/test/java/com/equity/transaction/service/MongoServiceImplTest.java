package com.equity.transaction.service;

import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.repository.MarketPriceRepository;
import com.equity.transaction.service.repository.PositionRepository;
import com.equity.transaction.service.repository.TransactionRepository;
import com.equity.transaction.service.service.impl.MongoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class MongoServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private MarketPriceRepository marketPriceRepository;

    @InjectMocks
    private MongoServiceImpl mongoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveTransactions() {
        List<TransactionDTO> transactions = Arrays.asList(new TransactionDTO(), new TransactionDTO());
        mongoService.saveTransactions(transactions);
        verify(transactionRepository, times(1)).saveAll(transactions);
    }

    @Test
    public void testDeleteAllTransactions() {
        mongoService.deleteAllTransactions();
        verify(transactionRepository, times(1)).deleteAll();
    }

    @Test
    public void testSavePositions() {
        List<PositionDTO> positions = Arrays.asList(new PositionDTO(), new PositionDTO());
        mongoService.savePositions(positions);
        verify(positionRepository, times(1)).saveAll(positions);
    }

    @Test
    public void testDeleteAllPositions() {
        mongoService.deleteAllPositions();
        verify(positionRepository, times(1)).deleteAll();
    }

    @Test
    public void testSaveMarketPrices() {
        List<MarketPriceDTO> prices = Arrays.asList(new MarketPriceDTO(), new MarketPriceDTO());
        mongoService.saveMarketPrices(prices);
        verify(marketPriceRepository, times(1)).saveAll(prices);
    }

    @Test
    public void testDeleteAllMarketPrices() {
        mongoService.deleteAllMarketPrices();
        verify(marketPriceRepository, times(1)).deleteAll();
    }
}

