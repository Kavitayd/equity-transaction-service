package com.equity.transaction.service.service.impl;
import com.equity.transaction.service.repository.TransactionRepository;
import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.service.FileProcessingService;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class FileProcessingServiceImpl implements FileProcessingService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public List<TransactionDTO> readTransactionFromExcel(MultipartFile file) {
        List<TransactionDTO> transactions = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // First sheet
            Iterator<Row> rows = sheet.iterator();

            while (rows.hasNext()) {
                Row row = rows.next();

                if (row.getRowNum() == 0) {
                    continue; // skip header row
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
    public List<TransactionDTO> readTransactionFromCsv(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            ColumnPositionMappingStrategy<TransactionDTO> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(TransactionDTO.class);
            String[] columns = new String[]{"clientCode", "clientName", "eventType", "tradeDate", "settlementDate",
                    "securityCode", "quantity", "rate", "stampDuty", "sttBrokerage", "transactionCharges",
                    "turnoverFees", "clearingCharges", "GST"};
            strategy.setColumnMapping(columns);

            CsvToBean<TransactionDTO> csvToBean = new CsvToBeanBuilder<TransactionDTO>(reader)
                    .withType(TransactionDTO.class)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        } catch (IOException e) {
            throw new RuntimeException("Error parsing CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<TransactionDTO> transactionEntities = transactionRepository.findAll();
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
        dto.setSttBrokerage(entity.getSttBrokerage());
        dto.setTransactionCharges(entity.getTransactionCharges());
        dto.setTurnoverFees(entity.getTurnoverFees());
        dto.setClearingCharges(entity.getClearingCharges());
        dto.setGST(entity.getGST());
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
            return (int) cell.getNumericCellValue();  // fallback for formulas
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
    private LocalDate getDateValue(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            String dateStr = cell.getStringCellValue().trim();
            if (dateStr.isEmpty()) return null;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH);
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse date string: " + dateStr, e);
            }
        }
        return null;
    }

    private Double getDoubleValue(Cell cell) {
        return cell != null ? cell.getNumericCellValue() : null;
    }

//    private LocalDate getDateValue(Cell cell) {
//        return cell != null && DateUtil.isCellDateFormatted(cell)
//                ? cell.getLocalDateTimeCellValue().toLocalDate()
//                : null;
//    }
}