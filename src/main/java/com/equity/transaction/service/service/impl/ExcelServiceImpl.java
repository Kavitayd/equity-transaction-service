package com.equity.transaction.service.service.impl;
import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.repository.MarketPriceRepository;
import com.equity.transaction.service.repository.PositionRepository;
import com.equity.transaction.service.repository.TransactionRepository;
import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.service.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.equity.transaction.service.service.util.ServiceUtil;


import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import static com.equity.transaction.service.service.util.ServiceUtil.*;
@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MarketPriceRepository marketPriceRepository;
    @Autowired
    private PositionRepository positionRepository;

    private boolean isHeaderRow(Row row) {
        Cell firstCell = row.getCell(0);
        return firstCell != null && firstCell.getCellType() == CellType.STRING && firstCell.getStringCellValue().trim().equalsIgnoreCase("Client Code");
    }

    @Override
    public List<TransactionDTO> readTransactionSheet(MultipartFile file) {
        List<TransactionDTO> transactions = new ArrayList<>();
        int serialNumber = 1;

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            while (rows.hasNext()) {
                Row row = rows.next();
                if (row == null || row.getCell(0) == null) continue;

                String firstCellVal = getStringValue(row.getCell(0));
                if (firstCellVal == null || firstCellVal.trim().equalsIgnoreCase("Client Code") || !firstCellVal.matches("\\d+")) {
                    continue;
                }

                try {
                    TransactionDTO dto = new TransactionDTO();
                    Long clientCode = getLongValue(row.getCell(0));
                    dto.setClientCode(clientCode);
                    dto.setClientName(getStringValue(row.getCell(1)));
                    dto.setEventType(getStringValue(row.getCell(2)));

                    LocalDate tradeDateLocal = getDateValue(row.getCell(3));
                    LocalDate settlementDateLocal = getDateValue(row.getCell(4));
                    java.sql.Date tradeDate = tradeDateLocal != null ? java.sql.Date.valueOf(tradeDateLocal) : null;
                    dto.setTradeDate(tradeDate);
                    dto.setSettlementDate(settlementDateLocal != null ? java.sql.Date.valueOf(settlementDateLocal) : null);

                    String securityCode = getStringValue(row.getCell(5));
                    Integer quantity = getIntegerValue(row.getCell(6));
                    Double rate = getDoubleValue(row.getCell(7));
                    Double amount = (quantity != null && rate != null) ? quantity * rate : null;

                    dto.setSecurityCode(securityCode);
                    dto.setQuantity(quantity != null ? quantity.doubleValue() : null);
                    //changing
                    //dto.setRate(rate != null ? rate.doubleValue() : null);
                    dto.setRate(rate);
                    dto.setStampDuty(getIntegerValue(row.getCell(8)));
                    dto.setStt(getIntegerValue(row.getCell(9)));
                    dto.setBrokerage(getIntegerValue(row.getCell(10)));
                    dto.setTransactionCharges(getIntegerValue(row.getCell(11)));
                    dto.setTurnoverFees(getIntegerValue(row.getCell(12)));
                    dto.setClearingCharges(getIntegerValue(row.getCell(13)));
                    dto.setGST(getIntegerValue(row.getCell(14)));
                    //change
                    //dto.setAmount(amount != null ? amount.doubleValue() : null);
                    dto.setAmount(amount);
                    String transactionNrd = getStringValue(row.getCell(16));
                    String prdFlag = getStringValue(row.getCell(17));
                    dto.setTransactionNrd(transactionNrd);
                    dto.setPrdFlag(prdFlag);
                    dto.setGainLoss(getStringValue(row.getCell(18)));

                    dto.setAvailableSaleQuantity(quantity != null ? quantity.doubleValue() : 0.0);
                    dto.setAvailableBuyQuantity(quantity != null ? quantity.doubleValue() : 0.0);

                    dto.setTransactionId(getStringValue(row.getCell(21)));
                    String transactionId = clientCode + "_" + securityCode + "_" + tradeDate + "_" + serialNumber++;
                    dto.setTransactionId(transactionId);

                    transactions.add(dto);

                } catch (Exception ex) {
                    System.out.printf("❌ Row %d: Error processing row - %s%n", row.getRowNum(), ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }

        return transactions;
    }

    @Override
    public List<PositionDTO> readPositionSheet(MultipartFile file) {
        List<PositionDTO> positions = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(1);
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isHeaderRow(row)) continue;

                try {
                    PositionDTO dto = new PositionDTO();
                    dto.setClientCode(getLongValue(row.getCell(0)));
                    dto.setDate(getDateValue(row.getCell(1)));
                    dto.setSecurityCode(getStringValue(row.getCell(2)));
                    dto.setQty(getIntegerValue(row.getCell(3)));
                    dto.setHoldingCost(getDoubleValue(row.getCell(4)));
                    dto.setAverageCostPerUnit(getDoubleValue(row.getCell(5)));
                    dto.setCorpActionQty(getIntegerValue(row.getCell(6)));
                    dto.setMarketPricePerUnitOnToday(getDoubleValue(row.getCell(7)));
                    dto.setMarketValueOnToday(getDoubleValue(row.getCell(8)));
                    dto.setCumulativeUnrealisedGainLossUptoToday(getDoubleValue(row.getCell(9)));
                    positions.add(dto);
                } catch (Exception ex) {
                    System.out.printf("❌ Row %d: Error processing Position row - %s%n", row.getRowNum(), ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Positions sheet", e);
        }
        return positions;
    }

    @Override
    public List<MarketPriceDTO> readMarketPriceSheet(MultipartFile file) {
        List<MarketPriceDTO> prices = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(2);
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isHeaderRow(row)) continue;

                try {
                    MarketPriceDTO dto = new MarketPriceDTO();
                    dto.setSecurityCode(getStringValue(row.getCell(0)));
                    dto.setDate(getDateValue(row.getCell(1)));
                    dto.setPrice(getDoubleValue(row.getCell(2)));
                    prices.add(dto);
                } catch (Exception ex) {
                    System.out.printf("❌ Row %d: Error processing Market Price row - %s%n", row.getRowNum(), ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Market Price sheet", e);
        }
        return prices;
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream().map(this::convertTransactionsToDto).collect(Collectors.toList());
    }

    @Override
    public List<PositionDTO> getAllPositions() {
        return positionRepository.findAll().stream().map(this::convertPositionsToDto).collect(Collectors.toList());
    }

    @Override
    public List<MarketPriceDTO> getAllMarketPrices() {
        return marketPriceRepository.findAll().stream().map(this::convertMarketPriceToDto).collect(Collectors.toList());
    }

    private TransactionDTO convertTransactionsToDto(TransactionDTO entity) {
        TransactionDTO dto = new TransactionDTO();
        dto.setClientCode(entity.getClientCode());
        dto.setClientName(entity.getClientName());
        dto.setEventType(entity.getEventType());
        dto.setTradeDate(entity.getTradeDate());
        dto.setSettlementDate(entity.getSettlementDate());
        dto.setSecurityCode(entity.getSecurityCode());
        dto.setQuantity(entity.getQuantity());
        dto.setRate(entity.getRate());
        dto.setStampDuty(entity.getStampDuty());
        dto.setStt(entity.getStt());
        dto.setBrokerage(entity.getBrokerage());
        dto.setTransactionCharges(entity.getTransactionCharges());
        dto.setTurnoverFees(entity.getTurnoverFees());
        dto.setClearingCharges(entity.getClearingCharges());
        dto.setGST(entity.getGST());
        dto.setAmount(entity.getAmount());
        dto.setTransactionNrd(entity.getTransactionNrd());
        dto.setPrdFlag(entity.getPrdFlag());
        dto.setGainLoss(entity.getGainLoss());
        dto.setAvailableSaleQuantity(entity.getAvailableSaleQuantity());
        dto.setAvailableBuyQuantity(entity.getAvailableBuyQuantity());
        dto.setTransactionId(entity.getTransactionId());
        return dto;
    }

    private PositionDTO convertPositionsToDto(PositionDTO entity) {
        PositionDTO dto = new PositionDTO();
        dto.setClientCode(entity.getClientCode());
        dto.setDate(entity.getDate());
        dto.setSecurityCode(entity.getSecurityCode());
        dto.setQty(entity.getQty());
        dto.setHoldingCost(entity.getHoldingCost());
        dto.setAverageCostPerUnit(entity.getAverageCostPerUnit());
        dto.setCorpActionQty(entity.getCorpActionQty());
        dto.setMarketPricePerUnitOnToday(entity.getMarketPricePerUnitOnToday());
        dto.setMarketValueOnToday(entity.getMarketValueOnToday());
        dto.setCumulativeUnrealisedGainLossUptoToday(entity.getCumulativeUnrealisedGainLossUptoToday());
        return dto;
    }

    private MarketPriceDTO convertMarketPriceToDto(MarketPriceDTO entity) {
        MarketPriceDTO dto = new MarketPriceDTO();
        dto.setSecurityCode(entity.getSecurityCode());
        dto.setDate(entity.getDate());
        dto.setPrice(entity.getPrice());
        return dto;
    }
}
