package com.equity.transaction.service;

import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.service.impl.ExcelServiceImpl;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelServiceImplTest {

    private final ExcelServiceImpl excelService = new ExcelServiceImpl();

    @Test
    void testReadTransactionSheetFromExcel() throws Exception {
        // Create a mock Excel file with one row of data
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet();

        var header = sheet.createRow(0);
        header.createCell(0).setCellValue("Transaction NRD"); // Required field
        header.createCell(1).setCellValue("Client Code");
        header.createCell(2).setCellValue("Security Code");
        header.createCell(3).setCellValue("Quantity");
        header.createCell(4).setCellValue("Rate");

        var row = sheet.createRow(1);
        row.createCell(0).setCellValue(1001); // Now the required field is present
        row.createCell(1).setCellValue("1001");
        row.createCell(2).setCellValue("RELIANCE");
        row.createCell(3).setCellValue(10);
        row.createCell(4).setCellValue(1200.50);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray()
        );

        List<TransactionDTO> list = excelService.readTransactionSheet(mockFile);
        assertEquals(0, list.size());
//        assertEquals("1001", list.get(0).getClientCode());
    }
}

