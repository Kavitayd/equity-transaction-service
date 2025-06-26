package com.equity.transaction.service.service.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;

import static com.equity.transaction.service.service.util.ServiceUtil.*;

import static com.equity.transaction.service.service.util.ServiceUtil.roundToTwoDecimalPlaces;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ServiceUtil {


    /**
     * Extracts the string value from a cell, handling different cell types.
     */
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
    /**
     * Extracts an Integer value from a cell, supporting numeric, string, and formula types.
     */
    public static Integer getIntegerValue(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    if (str.matches("\\d+")) {
                        return Integer.parseInt(str);
                    } else {
                        throw new NumberFormatException("Cell contains non-numeric string: " + str);
                    }
                case FORMULA:
                    return (int) cell.getNumericCellValue();
                case BLANK:
                    return null;
                default:
                    throw new NumberFormatException("Unsupported cell type: " + cell.getCellType());
            }
        } catch (Exception e) {
            throw new NumberFormatException("Failed to parse cell to Integer: " + e.getMessage());
        }
    }


//    public static Integer getIntegerValue(Cell cell) {
//        if (cell == null) return null;
//        if (cell.getCellType() == CellType.NUMERIC) {
//            return (int) cell.getNumericCellValue();
//        } else if (cell.getCellType() == CellType.STRING) {
//            String str = cell.getStringCellValue().trim();
//            return str.isEmpty() ? null : Integer.parseInt(str);
//        } else if (cell.getCellType() == CellType.FORMULA) {
//            return (int) cell.getNumericCellValue();
//        }
//        return null;
//    }

    /**
     * Extracts a Long value from a cell (used for large numeric IDs like clientCode).
     */
    public static Long getLongValue(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    if (str.matches("\\d+")) {
                        return Long.parseLong(str);
                    } else {
                        throw new NumberFormatException("Cell contains non-numeric string: " + str);
                    }
                case FORMULA:
                    return (long) cell.getNumericCellValue();  // assume result is numeric
                case BLANK:
                    return null;
                default:
                    throw new NumberFormatException("Unsupported cell type: " + cell.getCellType());
            }
        } catch (Exception e) {
            throw new NumberFormatException("Failed to parse cell to Long: " + e.getMessage());
        }
    }

    /**
     * Extracts a Double value from a cell. Returns null if invalid.
     */
    public static Double getDoubleValue(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String str = cell.getStringCellValue().trim();
                return str.isEmpty() ? null : Double.parseDouble(str);
            } else if (cell.getCellType() == CellType.FORMULA) {
                return cell.getNumericCellValue();
            }
        } catch (NumberFormatException | IllegalStateException e) {
            System.out.println(" Skipping cell (expected Double): " + e.getMessage());
            return null;
        }

        return null;
    }

    /**
     * Returns a LocalDate from a date-formatted Excel cell.
     */
    public static LocalDate getDateValue(Cell cell) {
        return cell != null && DateUtil.isCellDateFormatted(cell)
                ? cell.getLocalDateTimeCellValue().toLocalDate()
                : null;
    }
    /**
     * Rounds a double value to two decimal places using HALF_UP rounding mode.
     */

    public static double roundToTwoDecimalPlaces(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    /**
     * Converts java.util.Date to java.time.LocalDate.
     */

    public static LocalDate convertToLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    //*******
    private static final Map<String, Integer> serialNumberMap = new HashMap<>();

    /**
     * Generates a unique transaction ID in the format:
     * <clientCode>_<eventType>_<securityCode>_<serialNumber>
     */
    public static String generateCustomTransactionId(Long clientCode, String eventType, String securityCode) {
        String key = clientCode + "_" + eventType + "_" + securityCode;
        int serial = serialNumberMap.getOrDefault(key, 0) + 1;
        serialNumberMap.put(key, serial);

        return key + "_" + serial;
    }


}

