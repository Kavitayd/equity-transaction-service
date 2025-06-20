package com.equity.transaction.service.service.impl;

import com.equity.transaction.service.model.CapitalGainBreakupDTO;
import com.equity.transaction.service.model.CapitalGainDTO;
import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.repository.TransactionRepository;
import com.equity.transaction.service.service.CapitalGainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.equity.transaction.service.service.MongoService;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CapitalGainServiceImpl implements CapitalGainService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MongoService mongoService;

    @Override
    public void computeCapitalGains() {

        // STEP 1: Get all transactions from MongoDB
        List<TransactionDTO> allTransactions = mongoService.getAllTransactions();
        System.out.println("Total transactions fetched: " + allTransactions.size());

        // STEP 2: Filter only EQ_SAL transactions (sale transactions)
        List<TransactionDTO> saleTransactions = allTransactions.stream()
                .filter(txn -> "EQ_SAL".equalsIgnoreCase(txn.getEventType()))
                .collect(Collectors.toList());
        System.out.println("Total sale transactions (EQ_SAL): " + saleTransactions.size());

        // STEP 3: Extract unique (ClientCode, SecurityCode, SettlementDate) keys for reference
        Set<String> uniqueSaleKeys = saleTransactions.stream()
                .map(txn -> txn.getClientCode() + "_" + txn.getSecurityCode() + "_" + txn.getSettlementDate())
                .collect(Collectors.toSet());
        System.out.println("Unique sale keys: " + uniqueSaleKeys.size());
        uniqueSaleKeys.forEach(System.out::println);

        // STEP 4: Proceed to FIFO gain/loss computation (next step)
        // We'll implement the matching logic using saleTransactions and allTransactions (which includes EQ_PUR)
        // This is just a placeholder for now
//        System.out.println("✅ Setup complete. Ready for FIFO gain/loss matching logic.");
    }
    @Override
    public List<CapitalGainDTO> computeCapitalGainsUsingFIFO() {

        System.out.println("FIFO logic coming soon...");
        List<TransactionDTO> allTransactions = mongoService.getAllTransactions();

        // Filter EQ_SAL transactions
        List<TransactionDTO> saleTransactions = allTransactions.stream()
                .filter(txn -> "EQ_SAL".equalsIgnoreCase(txn.getEventType()))
                .collect(Collectors.toList());

        // Placeholder: for each sale transaction, generate a dummy CapitalGainDTO
        List<CapitalGainDTO> results = new ArrayList<>();
        for (TransactionDTO sale : saleTransactions) {
            CapitalGainDTO gain = new CapitalGainDTO();
            gain.setClientCode(sale.getClientCode());
            gain.setSecurityCode(sale.getSecurityCode());
            gain.setSaleDate(sale.getSettlementDate());
            gain.setTotalCapitalGain(0.0); // placeholder
            gain.setBreakup(new ArrayList<>()); // empty list for now

            results.add(gain);
        }

        return results;
//        return new ArrayList<>(); // placeholder
    }

    @Override
    public Map<String, List<String>> getTransactionIdsGroupedByEventType() {
        List<TransactionDTO> allTransactions = mongoService.getAllTransactions();

        Map<String, List<String>> eventTypeToTransactionIds = allTransactions.stream()
                .collect(Collectors.groupingBy(
                        TransactionDTO::getEventType,
                        Collectors.mapping(TransactionDTO::getTransactionId, Collectors.toList())
                ));

        eventTypeToTransactionIds.forEach((eventType, ids) -> {
            System.out.println("Event Type: " + eventType + ", Transaction IDs: " + ids);
        });

        return eventTypeToTransactionIds;
    }
}

//    @Override
//    public List<CapitalGainDTO> computeCapitalGainsUsingFIFO() {
//        List<TransactionDTO> allTransactions = transactionRepository.findAll();
//
//        // Step 1: Filter sale transactions (EQ_SAL)
//        List<TransactionDTO> saleTransactions = allTransactions.stream()
//                .filter(txn -> "EQ_SAL".equalsIgnoreCase(txn.getEventType()))
//                .collect(Collectors.toList());
//
//        List<CapitalGainDTO> capitalGainResults = new ArrayList<>();
//
//        // Step 2: Process each sale transaction
//        for (TransactionDTO saleTxn : saleTransactions) {
//            Long clientCode = saleTxn.getClientCode();
//            String securityCode = saleTxn.getSecurityCode();
//            Date saleSettlementDate = saleTxn.getSettlementDate();
//
//            Integer remainingSaleQty = saleTxn.getAvailableSaleQuantity();
//            if (remainingSaleQty == null || remainingSaleQty == 0) continue;
//
//            // Step 3: Find matching purchase transactions using FIFO
//            List<TransactionDTO> matchingPurchases = allTransactions.stream()
//                    .filter(txn -> "EQ_PUR".equalsIgnoreCase(txn.getEventType()))
//                    .filter(txn -> txn.getClientCode().equals(clientCode))
//                    .filter(txn -> txn.getSecurityCode().equals(securityCode))
//                    .filter(txn -> txn.getSettlementDate() != null &&
//                            !txn.getSettlementDate().isAfter(saleSettlementDate))
//                    .filter(txn -> txn.getAvailableBuyQuantity() != null &&
//                            txn.getAvailableBuyQuantity() > 0)
//                    .sorted(Comparator.comparing(TransactionDTO::getSettlementDate))
//                    .collect(Collectors.toList());
//
//            List<CapitalGainBreakupDTO> breakupList = new ArrayList<>();
//
//            for (TransactionDTO purchaseTxn : matchingPurchases) {
//                if (remainingSaleQty <= 0) break;
//
//                Integer availableBuyQty = purchaseTxn.getAvailableBuyQuantity();
//                if (availableBuyQty == null || availableBuyQty <= 0) continue;
//
//                int matchedQty = Math.min(remainingSaleQty, availableBuyQty);
//
//                double purchaseRate = Optional.ofNullable(purchaseTxn.getRate()).orElse(0);
//                double saleRate = Optional.ofNullable(saleTxn.getRate()).orElse(0);
//
//                double gainOrLoss = matchedQty * (saleRate - purchaseRate);
//
//                CapitalGainBreakupDTO breakup = new CapitalGainBreakupDTO();
//                breakup.setClientCode(clientCode);
//                breakup.setSecurityCode(securityCode);
//                breakup.setPurchaseTransactionId(purchaseTxn.getTransactionId());
//                breakup.setSaleTransactionId(saleTxn.getTransactionId());
//                breakup.setPurchaseDate(purchaseTxn.getSettlementDate());
//                breakup.setSaleDate(saleTxn.getSettlementDate());
//                breakup.setPurchaseRate(purchaseRate);
//                breakup.setSaleRate(saleRate);
//                breakup.setQuantity((double) matchedQty);
//                breakup.setGainOrLoss(gainOrLoss);
//
//                breakupList.add(breakup);
//
//                // Update available quantities
//                purchaseTxn.setAvailableBuyQuantity(availableBuyQty - matchedQty);
//                saleTxn.setAvailableSaleQuantity(remainingSaleQty - matchedQty);
//                remainingSaleQty -= matchedQty;
//            }
//
//            if (!breakupList.isEmpty()) {
//                double totalGainLoss = breakupList.stream()
//                        .mapToDouble(CapitalGainBreakupDTO::getGainOrLoss)
//                        .sum();
//
//                CapitalGainDTO gainDto = new CapitalGainDTO();
//                gainDto.setSaleTransactionId(saleTxn.getTransactionId());
//                gainDto.setClientCode(clientCode);
//                gainDto.setSecurityCode(securityCode);
//                gainDto.setTotalGainOrLoss(totalGainLoss);
//                gainDto.setBreakups(breakupList);
//
//                capitalGainResults.add(gainDto);
//            }
//        }
//
//        return capitalGainResults;
//    }

