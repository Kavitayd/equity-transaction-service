package com.equity.transaction.service.controller;

import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.model.PositionDTO;
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
//
//@RestController
//@RequestMapping("/transactions")
//public class EquityTransactionController {
//
//    @Autowired
//    private ExcelService excelService;
//    @Autowired
//    private MongoService mongoService;
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadData(@RequestParam("file") MultipartFile file) {
//        try {
//            // Read transactions from Excel
//            List<TransactionDTO> transactions = excelService.readTransactionSheet(file);
//
//            mongoService.deleteAllTransactions();  // Delete old records
//            mongoService.saveTransactions(transactions);  // Save new ones
//
//            return ResponseEntity.ok("Transactions uploaded successfully.");
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Upload failed: " + e.getMessage());
//        }
//    }
//
//    @GetMapping
//    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
//        List<TransactionDTO> transactions = excelService.getAllTransactions();
//        return ResponseEntity.ok(transactions);
//    }
//}
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
            // Read all 3 sheets
            List<TransactionDTO> transactions = excelService.readTransactionSheet(file);     // Sheet 0
            List<PositionDTO> positions = excelService.readPositionSheet(file);             // Sheet 1
            List<MarketPriceDTO> marketPrices = excelService.readMarketPriceSheet(file);    // Sheet 2

            // Save them to MongoDB
            mongoService.deleteAllTransactions();
            mongoService.saveTransactions(transactions);

            mongoService.deleteAllPositions();
            mongoService.savePositions(positions);

            mongoService.deleteAllMarketPrices();
            mongoService.saveMarketPrices(marketPrices);

            return ResponseEntity.ok("All sheets uploaded and saved successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = excelService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/positions")
    public ResponseEntity<List<PositionDTO>> getAllPositions() {
        List<PositionDTO> positions = excelService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/market_price")
    public ResponseEntity<List<MarketPriceDTO>> getAllMarketPrices() {
        List<MarketPriceDTO> marketPrices = excelService.getAllMarketPrices();
        return ResponseEntity.ok(marketPrices);
    }
}