//package com.equity.transaction.service.mapper;
//
//import org.apache.poi.ss.usermodel.Row;
//import org.springframework.stereotype.Component;
//import com.equity.transaction.service.model.TransactionDTO;
//import java.util.Map;
//
//
//@Component
//public class TransactionExcelMapper {
//
//    public TransactionDTO mapRow(Row row) {
//        TransactionDTO dto = new TransactionDTO();
//
//        dto.setClientCode(getLongValue(row.getCell(0)));
//        dto.setClientName(getStringValue(row.getCell(1)));
//        dto.setEventType(getStringValue(row.getCell(2)));
//        dto.setTradeDate(getDateValue(row.getCell(3)));
//        dto.setSettlementDate(getDateValue(row.getCell(4)));
//        dto.setSecurityCode(getStringValue(row.getCell(5)));
//        dto.setQuantity(getIntegerValue(row.getCell(6)));
//        dto.setRate(getDoubleValue(row.getCell(7)));
//        dto.setStampDuty(getIntegerValue(row.getCell(8)));
//        dto.setSttBrokerage(getIntegerValue(row.getCell(9)));
//        dto.setTransactionCharges(getIntegerValue(row.getCell(10)));
//        dto.setTurnoverFees(getIntegerValue(row.getCell(11)));
//        dto.setClearingCharges(getIntegerValue(row.getCell(12)));
//        dto.setGST(getIntegerValue(row.getCell(13)));
//
//        return dto;
//    }
//
//    private String getStringValue(Cell cell) {
//        return cell != null ? cell.getStringCellValue().trim() : null;
//    }
//
//    private Integer getIntegerValue(Cell cell) {
//        return cell != null ? (int) cell.getNumericCellValue() : null;
//    }
//
//    private Long getLongValue(Cell cell) {
//        return cell != null ? (long) cell.getNumericCellValue() : null;
//    }
//
//    private Double getDoubleValue(Cell cell) {
//        return cell != null ? cell.getNumericCellValue() : null;
//    }
//
//    private LocalDate getDateValue(Cell cell) {
//        return (cell != null && DateUtil.isCellDateFormatted(cell))
//                ? cell.getLocalDateTimeCellValue().toLocalDate()
//                : null;
//    }
//}
