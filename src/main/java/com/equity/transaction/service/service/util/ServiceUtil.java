package com.equity.transaction.service.service.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.time.LocalDate;

public class ServiceUtil {

    public static String getStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula(); // or evaluate
            case BLANK:
                return "";
            default:
                return cell.toString();
        }
    }

    public static Integer getIntegerValue(Cell cell) {
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

    public static Long getLongValue(Cell cell) {
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

    public static Double getDoubleValue(Cell cell) {
        return cell != null ? cell.getNumericCellValue() : null;
    }

    public static LocalDate getDateValue(Cell cell) {
        return cell != null && DateUtil.isCellDateFormatted(cell)
                ? cell.getLocalDateTimeCellValue().toLocalDate()
                : null;
    }
}