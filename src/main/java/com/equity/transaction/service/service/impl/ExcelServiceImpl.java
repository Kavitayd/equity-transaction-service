package com.equity.transaction.service.service.impl;

import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.repository.MarketPriceRepository;
import com.equity.transaction.service.repository.PositionRepository;
import com.equity.transaction.service.repository.TransactionRepository;
import com.equity.transaction.service.service.ExcelService;
import com.equity.transaction.service.service.util.ServiceUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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

    // Column indices for transaction sheet
    private static final int COL_CLIENT_CODE = 0;
    private static final int COL_CLIENT_NAME = 1;
    private static final int COL_EVENT_TYPE = 2;
    private static final int COL_TRADE_DATE = 3;
    private static final int COL_SETTLEMENT_DATE = 4;
    private static final int COL_SECURITY_CODE = 5;
    private static final int COL_QUANTITY = 6;
    private static final int COL_RATE = 7;
    private static final int COL_STAMP_DUTY = 8;
    private static final int COL_STT = 9;
    private static final int COL_BROKERAGE = 10;
    private static final int COL_TRANS_CHARGES = 11;
    private static final int COL_TURNOVER_FEES = 12;
    private static final int COL_CLEARING_CHARGES = 13;
    private static final int COL_GST = 14;
    private static final int COL_NRD = 16;
    private static final int COL_PRD_FLAG = 17;
    private static final int COL_GAIN_LOSS = 18;
    private static final int COL_ISIN = 19;
    private static final int COL_SECURITY_NAME = 20;
    private static final int COL_LISTING_STATUS = 22;
    private static final int COL_PRD_HOLDING_FLAG = 23;

    // Sheet-specific header checks
    private boolean isTransactionHeader(Row row) {
        Cell cell = row.getCell(COL_CLIENT_CODE);
        return cell != null && cell.getCellType() == CellType.STRING &&
                cell.getStringCellValue().trim().equalsIgnoreCase("Client Code");
    }

    private boolean isPositionHeader(Row row) {
        Cell cell = row.getCell(0);
        return cell != null && cell.getCellType() == CellType.STRING &&
                cell.getStringCellValue().trim().equalsIgnoreCase("Client Code");
    }


    private boolean isMarketPriceHeader(Row row) {
        Cell cell = row.getCell(0);
        return cell != null && cell.getCellType() == CellType.STRING &&
                cell.getStringCellValue().trim().equalsIgnoreCase("Security Code");
    }

    @Override
    public List<TransactionDTO> readTransactionSheet(MultipartFile file) {
        List<TransactionDTO> transactions = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            System.out.println("Reading Sheet: " + sheet.getSheetName());

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isTransactionHeader(row)) continue;
                try {
                    TransactionDTO dto = new TransactionDTO();
                    dto.setClientCode(getLongValue(row.getCell(COL_CLIENT_CODE)));
                    dto.setClientName(getStringValue(row.getCell(COL_CLIENT_NAME)));
                    dto.setEventType(getStringValue(row.getCell(COL_EVENT_TYPE)));

                    LocalDate tradeDate = getDateValue(row.getCell(COL_TRADE_DATE));
                    LocalDate settlementDate = getDateValue(row.getCell(COL_SETTLEMENT_DATE));
                    dto.setTradeDate(Date.from(tradeDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    dto.setSettlementDate(Date.from(settlementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

                    Double quantity = getDoubleValue(row.getCell(COL_QUANTITY));
                    dto.setSecurityCode(getStringValue(row.getCell(COL_SECURITY_CODE)));
                    dto.setQuantity(quantity);
                    dto.setRate(getDoubleValue(row.getCell(COL_RATE)));
                    dto.setStampDuty(getIntegerValue(row.getCell(COL_STAMP_DUTY)));
                    dto.setStt(getIntegerValue(row.getCell(COL_STT)));
                    dto.setBrokerage(getIntegerValue(row.getCell(COL_BROKERAGE)));
                    dto.setTransactionCharges(getIntegerValue(row.getCell(COL_TRANS_CHARGES)));
                    dto.setTurnoverFees(getIntegerValue(row.getCell(COL_TURNOVER_FEES)));
                    dto.setClearingCharges(getIntegerValue(row.getCell(COL_CLEARING_CHARGES)));
                    dto.setGST(getIntegerValue(row.getCell(COL_GST)));
                    dto.setAmount((quantity != null && dto.getRate() != null) ? quantity * dto.getRate() : null);
                    dto.setTransactionNrd(getStringValue(row.getCell(COL_NRD)));
                    dto.setPrdFlag(getStringValue(row.getCell(COL_PRD_FLAG)));
                    dto.setGainLoss(getStringValue(row.getCell(COL_GAIN_LOSS)));
                    dto.setIsin(getStringValue(row.getCell(COL_ISIN)));

                    String secName = getStringValue(row.getCell(COL_SECURITY_NAME));
                    dto.setSecurityName((secName != null && !secName.matches("\\d+")) ? secName : null);
                    dto.setListingStatus(getStringValue(row.getCell(COL_LISTING_STATUS)));
                    dto.setPrdHoldingFlag(getStringValue(row.getCell(COL_PRD_HOLDING_FLAG)));

                    dto.setAvailableBuyQuantity(quantity != null ? quantity : 0.0);
                    dto.setAvailableSaleQuantity(quantity != null ? quantity : 0.0);
                    dto.setTransactionId(ServiceUtil.generateCustomTransactionId(dto.getClientCode(), dto.getEventType(), dto.getSecurityCode()));

                    transactions.add(dto);
                } catch (Exception ex) {
                    System.out.printf("Row %d: Error processing row - %s%n", row.getRowNum(), ex.getMessage());
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
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
              Sheet sheet = workbook.getSheetAt(1);
            //Sheet sheet = workbook.getSheet("Sheet1");
            System.out.println("Reading Sheet: " + sheet.getSheetName());

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                System.out.println("Sheet[" + i + "] = " + workbook.getSheetName(i));
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isPositionHeader(row)) continue;
                try {
                    PositionDTO dto = new PositionDTO();
                    dto.setClientCode(getLongValue(row.getCell(0)));                         // A
                    dto.setDate(getDateValue(row.getCell(1)));                               // B
                    dto.setSecurityCode(getStringValue(row.getCell(2)));                     // C
                    dto.setQty(getIntegerValue(row.getCell(3)));                             // D
                    dto.setHoldingCost(getDoubleValue(row.getCell(4)));                      // E
                    dto.setAverageCostPerUnit(getDoubleValue(row.getCell(5)));              // F
                    dto.setMarketPricePerUnitOnToday(getDoubleValue(row.getCell(6)));       // G
                    dto.setMarketValueOnToday(getDoubleValue(row.getCell(7)));              // H
                    dto.setCumulativeUnrealisedGainLossUptoToday(getDoubleValue(row.getCell(8)));  // I
                    dto.setMarketPricePerUnitT1day(getDoubleValue(row.getCell(9)));         // J
                    dto.setUnrealisedGainLossForToday(getDoubleValue(row.getCell(10)));
                    //dto.setCorpActionQty(getDoubleValue(row.getCell(11))); // if field exists in DTO

// Skipping column 11 (CorpActionQty) for now unless needed
                    positions.add(dto);

                } catch (Exception ex) {
                    System.out.printf("Row %d: Error processing Position row - %s%n", row.getRowNum(), ex.getMessage());
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
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(2);
            System.out.println("Reading Sheet: " + sheet.getSheetName());

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isMarketPriceHeader(row)) continue;
                try {
                    MarketPriceDTO dto = new MarketPriceDTO();
                    dto.setSecurityCode(getStringValue(row.getCell(0)));
                    dto.setDate(getDateValue(row.getCell(1)));
                    dto.setPrice(getDoubleValue(row.getCell(2)));
                    prices.add(dto);
                } catch (Exception ex) {
                    System.out.printf("Row %d: Error processing Market Price row - %s%n", row.getRowNum(), ex.getMessage());
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
        dto.setIsin(entity.getIsin());
        dto.setSecurityName(entity.getSecurityName());
        dto.setListingStatus(entity.getListingStatus());
        dto.setPrdHoldingFlag(entity.getPrdHoldingFlag());
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
        dto.setMarketPricePerUnitOnToday(entity.getMarketPricePerUnitOnToday());
        dto.setMarketValueOnToday(entity.getMarketValueOnToday());
        dto.setCumulativeUnrealisedGainLossUptoToday(entity.getCumulativeUnrealisedGainLossUptoToday());
        dto.setUnrealisedGainLossForToday(entity.getUnrealisedGainLossForToday()); // ✅ Added missing field
        dto.setMarketPricePerUnitT1day(entity.getMarketPricePerUnitT1day());
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
