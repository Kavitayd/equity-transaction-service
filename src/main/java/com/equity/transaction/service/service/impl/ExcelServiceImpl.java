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
        int serialNumber = 1;

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
                Long clientCode = getLongValue(row.getCell(0));
                String securityCode = getStringValue(row.getCell(5));
                LocalDate tradeDate = getDateValue(row.getCell(3));

                Integer quantity = getIntegerValue(row.getCell(6));
                Integer rate = getIntegerValue(row.getCell(7));
//                Integer amount = (quantity != null && rate != null) ? (int) Math.round(quantity * rate) : null;
                Integer amount = (quantity != null && rate != null) ? quantity * rate : null;

                String prdFlag = getStringValue(row.getCell(17));

                String transactionNrd = getStringValue(row.getCell(16));

                int rowNum = row.getRowNum() + 1;

                if (transactionNrd == null || transactionNrd.isEmpty()) {
                    System.out.println("⚠️ Warning: Row " + rowNum + ": Transaction NRD is required but missing.");
                    continue;
                }

                if (prdFlag == null || prdFlag.isEmpty()) {
                    System.out.println("⚠️ Warning: Row " + rowNum + ": PRD Flag is required but missing.");
                    continue;
                }
                Integer saleQtyFromExcel = getIntegerValue(row.getCell(19));
                Integer buyQtyFromExcel = getIntegerValue(row.getCell(20));
                // Warning if manual entry differs from derived logic
                if (saleQtyFromExcel != null && !saleQtyFromExcel.equals(quantity)) {
                    System.out.printf("Warning: Row %d - Available Sale Quantity (%d) does not match Quantity (%d)%n", row.getRowNum(), saleQtyFromExcel, quantity);
                }

                if (buyQtyFromExcel != null && !buyQtyFromExcel.equals(quantity)) {
                    System.out.printf("Warning: Row %d - Available Buy Quantity (%d) does not match Quantity (%d)%n", row.getRowNum(), buyQtyFromExcel, quantity);
                }

                dto.setClientCode(clientCode);
                dto.setClientName(getStringValue(row.getCell(1)));
                dto.setEventType(getStringValue(row.getCell(2)));
                dto.setTradeDate(tradeDate);
                dto.setSettlementDate(getDateValue(row.getCell(4)));
                dto.setSecurityCode(securityCode);
                dto.setQuantity(quantity);
                dto.setRate(rate);
                dto.setStampDuty(getIntegerValue(row.getCell(8)));
                dto.setStt(getIntegerValue(row.getCell(9)));
                dto.setBrokerage(getIntegerValue(row.getCell(10)));
                dto.setTransactionCharges(getIntegerValue(row.getCell(11)));
                dto.setTurnoverFees(getIntegerValue(row.getCell(12)));
                dto.setClearingCharges(getIntegerValue(row.getCell(13)));
                dto.setGST(getIntegerValue(row.getCell(14)));
                dto.setAmount(amount);
                dto.setTransactionNrd(transactionNrd);
                dto.setPrdFlag(prdFlag);
                dto.setGainLoss(getStringValue(row.getCell(18)));
                dto.setAvailableSaleQuantity(quantity != null ? quantity : 0);
                dto.setAvailableBuyQuantity(quantity != null ? quantity : 0);
                dto.setTransactionId(getStringValue(row.getCell(21)));

                String transactionId = clientCode + "_" + securityCode + "_" + tradeDate + "_" + serialNumber++;
                dto.setTransactionId(transactionId);

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



    public String getStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Convert numeric to string, remove decimal if it's a whole number
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    // It's an integer
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula(); // or evaluate it
            case BLANK:
                return "";
            default:
                return cell.toString(); // fallback
        }
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