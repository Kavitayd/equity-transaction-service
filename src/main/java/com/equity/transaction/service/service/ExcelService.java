package com.equity.transaction.service.service;

import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.model.TransactionDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ExcelService {
    List<TransactionDTO> readTransactionSheet(MultipartFile file);
    List<PositionDTO> readPositionSheet(MultipartFile file);
    List<MarketPriceDTO> readMarketPriceSheet(MultipartFile file);

    List<TransactionDTO> getAllTransactions();
    List<PositionDTO> getAllPositions();
    List<MarketPriceDTO> getAllMarketPrices();
}

