//package com.equity.transaction.service.service.impl;
//
//import com.equity.transaction.service.model.CapitalGainDTO;
//import com.equity.transaction.service.model.TransactionDTO;
//import com.equity.transaction.service.repository.TransactionRepository;
//import com.equity.transaction.service.service.EquityComputationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.sql.Date;
//import java.util.*;
//
//@Service
//public class EquityComputationServiceImpl implements EquityComputationService {
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Override
//    public void computeGainLossFIFO() {
//        // Step 1: Get all sale transactions
//        List<TransactionDTO> saleTransactions = transactionRepository.findByEventType("EQ_SAL");
//
//        for (TransactionDTO saleTx : saleTransactions) {
//            int remainingSaleQty = saleTx.getAvailableSaleQuantity();
//            double totalGainLoss = 0;
//            List<CapitalGainDTO> breakupList = new ArrayList<>();
//
//            // Convert LocalDate to java.util.Date
//            Date saleSettlementDate = new java.sql.Date(saleTx.getSettlementDate().getTime());
//
//
//            // Step 2: Find matching purchase transactions for this sale
//            List<TransactionDTO> purchaseTxList =
//                    transactionRepository.findByEventTypeAndClientCodeAndSecurityCodeAndSettlementDateLessThanEqualAndAvailableBuyQuantityGreaterThan(
//                            "EQ_PUR",
//                            saleTx.getClientCode().intValue(), // Convert Long → int
//                            saleTx.getSecurityCode(),
//                            saleTx.getSettlementDate(),        // Already java.util.Date — use directly
//                            0
//                    );
//
//
//
//            // Step 3: Sort purchases FIFO (by earliest settlement date)
//            purchaseTxList.sort(Comparator.comparing(TransactionDTO::getSettlementDate));
//
//            // Step 4: Loop over purchases and match quantities using FIFO
//            for (TransactionDTO purTx : purchaseTxList) {
//                int availableBuyQty = purTx.getAvailableBuyQuantity();
//                if (availableBuyQty == 0) continue;
//
//                int matchedQty = Math.min(remainingSaleQty, availableBuyQty);
//                double gain = (saleTx.getRate() - purTx.getRate()) * matchedQty;
//
//                // Update available buy quantity in the purchase transaction
//                purTx.setAvailableBuyQuantity(availableBuyQty - matchedQty);
//                transactionRepository.save(purTx);
//
//                remainingSaleQty -= matchedQty;
//                totalGainLoss += gain;
//
//                // Prepare gain/loss breakup entry
//                CapitalGainDTO gainDTO = new CapitalGainDTO();
//                gainDTO.setSaleTransactionId(saleTx.getTransactionId());
//                gainDTO.setPurchaseTransactionId(purTx.getTransactionId());
//                gainDTO.setMatchedQuantity(matchedQty);
//                gainDTO.setPurchaseRate(purTx.getRate());
//                gainDTO.setSaleRate(saleTx.getRate());
//                gainDTO.setGainOrLoss(gain);
//
//                breakupList.add(gainDTO);
//
//                // Exit loop if sale quantity is fully matched
//                if (remainingSaleQty == 0) break;
//            }
//
//            // Step 5: Update sale transaction with gain/loss and remaining quantity
//            saleTx.setAvailableSaleQuantity(remainingSaleQty);
//            saleTx.setGainOrLoss(totalGainLoss);
//            transactionRepository.save(saleTx);
//
//            // Step 6 (Optional): Save gain/loss breakup list to another collection
//            // TODO: Uncomment and use if you have a repository for capital gains
//            // capitalGainRepository.saveAll(breakupList);
//        }
//    }
//}
