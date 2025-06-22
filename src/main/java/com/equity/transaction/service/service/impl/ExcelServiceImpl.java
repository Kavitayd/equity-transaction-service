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

    /**
     * Helper method to check if the given row is header row.
     * It assumes the first column (index 0) contains the string "Client code" in the header row.
     *
     * @param row The Excel row to be checked
     * @return true if the row is a header row, false otherwise
     */

    private boolean isHeaderRow(Row row) {
        Cell firstCell = row.getCell(0);
        if (firstCell == null) return false;
        if (firstCell.getCellType() != CellType.STRING) return false;
        String value = firstCell.getStringCellValue().trim().toLowerCase();
        return value.equals("client code");
    }

    /**
     * Reads the first sheet (Sheet0) from the uplode Excel file and maps each row to a transactionDTO object.
     * Skips the header row and assign a serial no to help generate unique transaction IDs
     *
     * @param file Multipart file uploaded via API (Excel format expected)
     * @return List of TransactionDTO objects populated from the Excel sheet
     */
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

                //Create a new TransnsactionDto object to hold parsed data for the current row
                TransactionDTO dto = new TransactionDTO();
                Long clientCode = getLongValue(row.getCell(0));
                String securityCode = getStringValue(row.getCell(5));
                LocalDate tradeDateLocal = getDateValue(row.getCell(3));
                java.sql.Date tradeDate = tradeDateLocal != null ? java.sql.Date.valueOf(tradeDateLocal) : null;

                LocalDate settlementDateLocal = getDateValue(row.getCell(4));
                java.sql.Date settlementDate = settlementDateLocal != null ? java.sql.Date.valueOf(settlementDateLocal) : null;

                Integer quantity = getIntegerValue(row.getCell(6));
                Integer rate = getIntegerValue(row.getCell(7));
                Integer amount = (quantity != null && rate != null) ? quantity * rate : null;

                String prdFlag = getStringValue(row.getCell(17));
                String transactionNrd = getStringValue(row.getCell(16));
                int rowNum = row.getRowNum() + 1;

                //Validate: Transaction NRD must be present
                if (transactionNrd == null || transactionNrd.isEmpty()) {
                    System.out.println("⚠️ Warning: Row " + rowNum + ": Transaction NRD is required but missing.");
                    continue;
                }

                //Validate PRD Flag must be present
                if (prdFlag == null || prdFlag.isEmpty()) {
                    System.out.println("⚠️ Warning: Row " + rowNum + ": PRD Flag is required but missing.");
                    continue;
                }

                //Validate that aAvailable Sale/Buy Quantity matches Quantity from the sheet
                Integer saleQtyFromExcel = getIntegerValue(row.getCell(19));
                Integer buyQtyFromExcel = getIntegerValue(row.getCell(20));
                // Warning if manual entry differs from derived logic
                if (saleQtyFromExcel != null && !saleQtyFromExcel.equals(quantity)) {
                    System.out.printf("Warning: Row %d - Available Sale Quantity (%d) does not match Quantity (%d)%n", row.getRowNum(), saleQtyFromExcel, quantity);
                }

                //Show warning if availabe Buy Quantity doesn't match Quantity
                if (buyQtyFromExcel != null && !buyQtyFromExcel.equals(quantity)) {
                    System.out.printf("Warning: Row %d - Available Buy Quantity (%d) does not match Quantity (%d)%n", row.getRowNum(), buyQtyFromExcel, quantity);
                }

                dto.setClientCode(clientCode);
                dto.setClientName(getStringValue(row.getCell(1)));
                dto.setEventType(getStringValue(row.getCell(2)));
                //dto.setTradeDate(tradeDate);
                //dto.setSettlementDate(getDateValue(row.getCell(4)));
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

    /**
     * Retrieves all transaction records from the MongoDB database,
     * converts them from entity format to DTO format, and returns them as a list.
     *
     * @return List of TransactionDTO object
     */
    @Override
    public List<TransactionDTO> getAllTransactions() {
        //Fetch all the Transaction documents from the MongoDb collection
        List<TransactionDTO> transactionEntities = transactionRepository.findAll();

        //Converts each entity to its corresponding DTO using a mapping method (convertTransactionToDTO)
        return transactionEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TransactionDTO convertToDto(TransactionDTO entity) {
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

    /**
     * Read the second sheet (sheet1 ) from the uploded Excel file,
     * parses each row into a PositionDTO object,and returns the list.
     *
     * @param file Excel file uploaded via the API (expected to contain Positions data in Sheet 1)
     * @return List of PositionDTO objects populated from the Excel sheet
     */

    @Override
    public List<PositionDTO> readPositionSheet(MultipartFile file) {
        List<PositionDTO> positions = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(1); // Sheet 1: Positions
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isHeaderRow(row)) continue;

                PositionDTO dto = new PositionDTO();
                //Map Excel cell to positionDTO fields using custom help methods
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

                //Add the populated DTO to the list
                positions.add(dto);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Positions sheet", e);
        }
        return positions;
    }

    /**
     * Fetches all Position records from the MongoDB repository,
     * converts each entity to a PositionDTO, and returns the list.
     *
     * @return List of PositionDTOs
     */
    @Override
    public List<PositionDTO> getAllPositions() {
        //Fetch all position documents from MongoDb
        List<PositionDTO> positionEntities = positionRepository.findAll();
        return positionEntities.stream()
                .map(this::convertPositionsToDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert a PositionDTO entity to a PositionDTO DTO.
     * In this case, both the input and output are the same class,
     * so this method may seem redundant but can be useful if transformation is added later.
     *
     * @param entity the Position entity fetched from the database
     * @return a new PositionDTO instance populated with the same data
     */

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

    /**
     * Reads the third sheet (Sheet 2) from the uploaded Excel file,
     * parses each row into a MarketPriceDTO object, and returns the list.
     *
     * @param file MultipartFile representing the uploaded Excel file
     * @return List of MarketPriceDTO records extracted from the Excel sheet
     */

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
}