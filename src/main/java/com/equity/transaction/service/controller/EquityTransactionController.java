package com.equity.transaction.service.controller;

import com.equity.transaction.service.model.CapitalGainDTO;
import com.equity.transaction.service.model.MarketPriceDTO;
import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.service.ExcelService;
import com.equity.transaction.service.service.CapitalGainService;
import com.equity.transaction.service.service.MongoService;
import com.equity.transaction.service.service.PositionService;
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

    @Autowired
    private PositionService positionService;

    // Upload Excel File and Process All Sheets
    @PostMapping("/upload")
    public ResponseEntity<String> uploadData(@RequestParam("file") MultipartFile file) {
        try {
            // Read all sheets
            List<TransactionDTO> transactions = excelService.readTransactionSheet(file);     // Sheet 0
            List<PositionDTO> positions = excelService.readPositionSheet(file);             // Sheet 1
            List<MarketPriceDTO> marketPrices = excelService.readMarketPriceSheet(file);    // Sheet 2

            logger.info("✅ Parsed {} transactions, {} positions, {} market prices",
                    transactions.size(), positions.size(), marketPrices.size());

            // Save all to MongoDB
            mongoService.deleteAllTransactions();
            mongoService.saveTransactions(transactions);
            logger.info("✅ Saved transactions to MongoDB");

            mongoService.deleteAllPositions();
            mongoService.savePositions(positions);
            logger.info("✅ Saved positions to MongoDB");

            mongoService.deleteAllMarketPrices();
            mongoService.saveMarketPrices(marketPrices);
            logger.info("✅ Saved market prices to MongoDB");

            return ResponseEntity.ok("All sheets uploaded and saved successfully.");

        } catch (Exception e) {
            logger.error(" Upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    // FIFO Gain/Loss API
    @GetMapping("/fifo")
    public ResponseEntity<List<CapitalGainDTO>> computeFifoCapitalGains() {
        List<CapitalGainDTO> gains = capitalGainService.computeCapitalGainsUsingFIFO();
        return ResponseEntity.ok(gains);
    }

    // Compute Position Master
    @PostMapping("/compute-positions")
    public ResponseEntity<String> computePositions() {
        try {
            positionService.computePositionMaster();
            return ResponseEntity.ok("Position Master computation completed successfully.");
        } catch (Exception e) {
            logger.error("Error during Position Master computation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error computing Position Master: " + e.getMessage());
        }
    }

    // Get All Transactions
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        try {
            List<TransactionDTO> transactions = excelService.getAllTransactions();
            logger.info("🔎 Retrieved {} transactions from MongoDB", transactions.size());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error while fetching transactions from MongoDB", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Health Check
    @GetMapping("/ping")
    public ResponseEntity<String> pingMongo() {
        try {
            mongoService.getAllTransactions(); // just a fetch
            return ResponseEntity.ok("✅ MongoDB is reachable!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(" MongoDB connection failed: " + e.getMessage());
        }
    }

    // Get All Positions
    @GetMapping("/positions")
    public ResponseEntity<List<PositionDTO>> getAllPositions() {
        try {
            List<PositionDTO> positions = excelService.getAllPositions();
            logger.info("🔎 Retrieved {} positions from MongoDB", positions.size());
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            logger.error("Error while fetching positions from MongoDB", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get All Market Prices
    @GetMapping("/market_price")
    public ResponseEntity<List<MarketPriceDTO>> getAllMarketPrices() {
        try {
            List<MarketPriceDTO> marketPrices = excelService.getAllMarketPrices();
            logger.info("🔎 Retrieved {} market prices from MongoDB", marketPrices.size());
            return ResponseEntity.ok(marketPrices);
        } catch (Exception e) {
            logger.error("Error while fetching market prices from MongoDB", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
