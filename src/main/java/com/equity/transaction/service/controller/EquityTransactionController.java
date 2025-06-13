package com.equity.transaction.service.controller;

import com.equity.transaction.service.model.CapitalGainDTO;
import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.model.TransactionDTO;
//import com.equity.transaction.service.service.EquityComputationService;
import com.equity.transaction.service.service.ExcelService;
import com.equity.transaction.service.service.CapitalGainService;
import com.equity.transaction.service.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/transactions")
public class EquityTransactionController {
    private static final Logger logger = LoggerFactory.getLogger(EquityTransactionController.class);

    @Autowired
    private ExcelService excelService;

    @Autowired
    private MongoService mongoService;

    @Autowired
    private CapitalGainService capitalGainService;

    //End point upload and process an Excel files contain three sheets of data
    @PostMapping("/upload")
    public ResponseEntity<String> uploadData(@RequestParam("file") MultipartFile file) {
        try {
            // Read all 3 sheets
            List<TransactionDTO> transactions = excelService.readTransactionSheet(file);     // Sheet 0
            List<PositionDTO> positions = excelService.readPositionSheet(file);             // Sheet 1
            List<MarketPriceDTO> marketPrices = excelService.readMarketPriceSheet(file);    // Sheet 2

            // Save them to MongoDB
            //Clear old transaction data and save new transaction to the MongoDb
            mongoService.deleteAllTransactions();
            mongoService.saveTransactions(transactions);

            //Clear old position data and save new position to MongoDb
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
    @GetMapping("/fifo")
    public ResponseEntity<List<CapitalGainDTO>> computeFifoCapitalGains() {
        List<CapitalGainDTO> gains = capitalGainService.computeCapitalGainsUsingFIFO();
        return ResponseEntity.ok(gains);
    }

    // Endpoint to get all the transaction records from MongoDB
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        try {
            List<TransactionDTO> transactions = excelService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error while fetching transaction from MongoDB",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Or you can use ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint to get all the position records from MongoDB
    @GetMapping("/positions")
    public ResponseEntity<List<PositionDTO>> getAllPositions() {
        try {
            List<PositionDTO> positions = excelService.getAllPositions();
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            logger.error("Error while fetching from MongoDB",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Endpoint to get all the market price records from MongoDB
    @GetMapping("/market_price")
    public ResponseEntity<List<MarketPriceDTO>> getAllMarketPrices() {
        try {
            List<MarketPriceDTO> marketPrices = excelService.getAllMarketPrices();
            return ResponseEntity.ok(marketPrices);
        } catch (Exception e) {
            logger.error("Error while fetching market price from MondoDB",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}

//    @Autowired
//    private EquityComputationService equityComputationService;

//    @PostMapping("/compute-fifo-gain-loss")
//    public ResponseEntity<String> computeFifo() {
//        equityComputationService.computeGainLossFIFO();
//        return ResponseEntity.ok("FIFO Gain/Loss Computation Completed");
//    }