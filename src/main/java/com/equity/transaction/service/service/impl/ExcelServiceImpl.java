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

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
        if (firstCell == null) return false;
        if (firstCell.getCellType() != CellType.STRING) return false;
        String value = firstCell.getStringCellValue().trim().toLowerCase();
        return value.equals("client code");
    }

    @Override
    public List<TransactionDTO> readTransactionSheet(MultipartFile file) {
        List<TransactionDTO> transactions = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // First sheet
            Iterator<Row> rows = sheet.iterator();

            while (rows.hasNext()) {
                Row row = rows.next();

                if (row.getRowNum() == 0 || isHeaderRow(row)) {
                    continue;
                }

                TransactionDTO dto = new TransactionDTO();
                dto.setClientCode(getLongValue(row.getCell(0)));
                dto.setClientName(getStringValue(row.getCell(1)));
                dto.setEventType(getStringValue(row.getCell(2)));
                dto.setTradeDate(getDateValue(row.getCell(3)));
                dto.setSettlementDate(getDateValue(row.getCell(4)));
                dto.setSecurityCode(getStringValue(row.getCell(5)));
                dto.setQuantity(getIntegerValue(row.getCell(6)));
                dto.setRate(getDoubleValue(row.getCell(7)));
                dto.setStampDuty(getIntegerValue(row.getCell(8)));
                dto.setSttBrokerage(getIntegerValue(row.getCell(9)));
                dto.setTransactionCharges(getIntegerValue(row.getCell(10)));
                dto.setTurnoverFees(getIntegerValue(row.getCell(11)));
                dto.setClearingCharges(getIntegerValue(row.getCell(12)));
                dto.setGST(getIntegerValue(row.getCell(13)));

                transactions.add(dto);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }

        return transactions;
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<TransactionDTO> transactionEntities = transactionRepository.findAll();
        return transactionEntities.stream()
                .map(this::convertTransactionsToDto)
                .collect(Collectors.toList());
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
        dto.setSttBrokerage(entity.getSttBrokerage());
        dto.setTransactionCharges(entity.getTransactionCharges());
        dto.setTurnoverFees(entity.getTurnoverFees());
        dto.setClearingCharges(entity.getClearingCharges());
        dto.setGST(entity.getGST());
        return dto;
    }

    @Override
    public List<PositionDTO> readPositionSheet(MultipartFile file) {
        List<PositionDTO> positions = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(1); // Sheet 1: Positions
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isHeaderRow(row)) continue;

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
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Positions sheet", e);
        }
        return positions;
    }

    @Override
    public List<PositionDTO> getAllPositions() {
        List<PositionDTO> positionEntities = positionRepository.findAll();
        return positionEntities.stream()
                .map(this::convertPositionsToDto)
                .collect(Collectors.toList());
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

    @Override
    public List<MarketPriceDTO> readMarketPriceSheet(MultipartFile file) {
        List<MarketPriceDTO> prices = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(2); // Sheet 2: Market Prices
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isHeaderRow(row)) continue;

                MarketPriceDTO dto = new MarketPriceDTO();
                dto.setSecurityCode(getStringValue(row.getCell(0)));
                dto.setDate(getDateValue(row.getCell(1)));
                dto.setPrice(getDoubleValue(row.getCell(2)));
                prices.add(dto);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Market Price sheet", e);
        }
        return prices;
    }
    @Override
    public List<MarketPriceDTO> getAllMarketPrices() {
        List<MarketPriceDTO> marketEntities = marketPriceRepository.findAll();
        return marketEntities.stream()
                .map(this::convertMarketPriceToDto)
                .collect(Collectors.toList());
    }

    private MarketPriceDTO convertMarketPriceToDto(MarketPriceDTO entity) {
        MarketPriceDTO dto = new MarketPriceDTO();
        dto.setSecurityCode(entity.getSecurityCode());
        dto.setDate(entity.getDate());
        dto.setPrice(entity.getPrice());
        return dto;

    }



    private String getStringValue(Cell cell) {
        return cell != null ? cell.getStringCellValue() : null;
    }

    private Integer getIntegerValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String str = cell.getStringCellValue().trim();
            return str.isEmpty() ? null : Integer.parseInt(str);
        } else if (cell.getCellType() == CellType.FORMULA) {
            return (int) cell.getNumericCellValue();
        }
        return null;
    }

    private Long getLongValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String str = cell.getStringCellValue().trim();
            return str.isEmpty() ? null : Long.parseLong(str);
        } else if (cell.getCellType() == CellType.FORMULA) {
            return (long) cell.getNumericCellValue();
        }
        return null;
    }

    private Double getDoubleValue(Cell cell) {
        return cell != null ? cell.getNumericCellValue() : null;
    }

    private LocalDate getDateValue(Cell cell) {
        return cell != null && DateUtil.isCellDateFormatted(cell)
                ? cell.getLocalDateTimeCellValue().toLocalDate()
                : null;
    }
}