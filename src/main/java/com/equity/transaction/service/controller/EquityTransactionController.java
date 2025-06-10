package com.equity.transaction.service.controller;

import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.service.ExcelService;
import com.equity.transaction.service.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Executable;
import java.util.List;
@RestController
@RequestMapping("/transactions")
public class EquityTransactionController {

    @Autowired
    private ExcelService excelService;
    @Autowired
    private MongoService mongoService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadData(@RequestParam("file") MultipartFile file) {
        try {
            List<TransactionDTO> transactions = excelService.readTransactionSheet(file);
            mongoService.saveTransactions(transactions);
            return ResponseEntity.ok("Transactions uploaded successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = excelService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
}
