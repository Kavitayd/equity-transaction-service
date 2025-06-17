package com.equity.transaction.service.service.util;
import com.opencsv.bean.AbstractBeanField;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter extends AbstractBeanField<LocalDate, String> {

    @Override
    protected LocalDate convert(String value) {
        System.out.println("Parsing date: " + value);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy");
        return LocalDate.parse(value.trim(), formatter);
    }
}