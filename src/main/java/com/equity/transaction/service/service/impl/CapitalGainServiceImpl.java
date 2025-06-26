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
import static com.equity.transaction.service.service.util.ServiceUtil.roundToTwoDecimalPlaces;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class CapitalGainServiceImpl implements CapitalGainService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MongoService mongoService;


    /**
     * Computes capital gains using FIFO (First-In-First-Out) matching of purchase and sale transactions.
     * Matches each sale transaction against the earliest available purchase transactions (FIFO order).
     */
    @Override
    public List<CapitalGainDTO> computeCapitalGainsUsingFIFO() {

        //Fetch all transactions from MongoDB
        List<TransactionDTO> allTransactions = mongoService.getAllTransactions();

        //Filter purchase and sale transactions by settlement date and sort them
        List<TransactionDTO> purchaseTransactions = allTransactions.stream()
                .filter(txn -> "EQ_PUR".equalsIgnoreCase(txn.getEventType()))
                .filter(txn -> txn.getSettlementDate() != null)
                .sorted(Comparator.comparing(TransactionDTO::getSettlementDate))
                .collect(Collectors.toList());

        //Filter and sort sale transactions by settlement date
        List<TransactionDTO> saleTransactions = allTransactions.stream()
                .filter(txn -> "EQ_SAL".equalsIgnoreCase(txn.getEventType()))
                .filter(txn -> txn.getSettlementDate() != null)
                .sorted(Comparator.comparing(TransactionDTO::getSettlementDate))
                .collect(Collectors.toList());

        List<CapitalGainDTO> capitalGainResults = new ArrayList<>();

        //Loop through each sale transaction and match it with purchase transactions
        for (TransactionDTO sale : saleTransactions) {
            double saleQtyRemaining = sale.getQuantity();
            List<CapitalGainBreakupDTO> breakupList = new ArrayList<>();
            double totalCapitalGain = 0;

            for (TransactionDTO purchase : purchaseTransactions) {
                //Skip mismatched client or security
                if (!Objects.equals(sale.getClientCode(), purchase.getClientCode()) ||
                        !Objects.equals(sale.getSecurityCode(), purchase.getSecurityCode())) {
                    continue;
                }
                //Skip exhausted purchase
                if (purchase.getAvailableBuyQuantity() == null || purchase.getAvailableBuyQuantity() <= 0) {
                    continue;
                }

                double matchedQty = Math.min(saleQtyRemaining, purchase.getAvailableBuyQuantity());
                if (matchedQty <= 0) continue;

                double gain = (sale.getRate() - purchase.getRate()) * matchedQty;

                long holdingDays = ChronoUnit.DAYS.between(
                        purchase.getSettlementDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        sale.getSettlementDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                );

                String gainType = holdingDays > 365 ? "Long Term Capital Gain" : "Short Term Capital Gain";
                // Create a breakup entry for matched quantities
                CapitalGainBreakupDTO breakup = new CapitalGainBreakupDTO();
                breakup.setPurchaseTransactionId(purchase.getTransactionId());
                breakup.setSaleTransactionId(sale.getTransactionId());

                breakup.setClientCode(sale.getClientCode());
                breakup.setSecurityCode(sale.getSecurityCode());
                breakup.setClientName(sale.getClientName());
                breakup.setSecurityName(sale.getSecurityName());
                breakup.setIsin(sale.getIsin());
                breakup.setListingStatus(sale.getListingStatus());
                breakup.setPrdHoldingFlag(sale.getPrdHoldingFlag());
                breakup.setCapitalGainType(gainType);

                breakup.setPurchaseDate(purchase.getSettlementDate());
                breakup.setSaleDate(sale.getSettlementDate());
                breakup.setClientName(sale.getClientName());
                breakup.setSecurityName(sale.getSecurityName());
                breakup.setIsin(sale.getIsin());
                breakup.setListingStatus(sale.getListingStatus());
                breakup.setPrdHoldingFlag(sale.getPrdHoldingFlag());
                breakup.setCapitalGainType(gainType); // already set if you’ve handled holding period

                breakup.setPurchasePrice(purchase.getRate().doubleValue());
                breakup.setSalePrice(sale.getRate().doubleValue());
                breakup.setQuantity(matchedQty);
                breakup.setGainOrLoss(gain);
                breakup.setHoldingPeriodDays((int) holdingDays);
                breakup.setClientCode(sale.getClientCode());
                breakup.setSecurityCode(sale.getSecurityCode());
                breakup.setClientName(sale.getClientName());
                breakup.setIsin(sale.getIsin());
                breakup.setSecurityName(sale.getSecurityName());
                breakup.setListingStatus(sale.getListingStatus());
                breakup.setPrdHoldingFlag(sale.getPrdHoldingFlag());
                breakup.setCapitalGainType(gainType);
                //breakup.setPurchasePrice(purchase.getRate() * matchedQty);
                //breakup.setSalePrice(sale.getRate() * matchedQty);
                breakup.setPurchaseValue(purchase.getRate() * matchedQty);
                breakup.setSaleValue(sale.getRate() * matchedQty);

                purchase.setAvailableBuyQuantity(purchase.getAvailableBuyQuantity() - matchedQty);
                saleQtyRemaining -= matchedQty;

                breakup.setBalancePurchaseQty(purchase.getAvailableBuyQuantity());
                breakup.setBalanceSaleQty(saleQtyRemaining);

                breakupList.add(breakup);
                totalCapitalGain += gain;

                // Reduce available quantity
//                purchase.setAvailableBuyQuantity(purchase.getAvailableBuyQuantity() - matchedQty);
//                saleQtyRemaining -= matchedQty;

                if (saleQtyRemaining <= 0) break;
            }

            CapitalGainDTO gainDTO = new CapitalGainDTO();
            gainDTO.setClientCode(sale.getClientCode());
            gainDTO.setSecurityCode(sale.getSecurityCode());
            gainDTO.setSaleDate(sale.getSettlementDate());
            gainDTO.setTotalCapitalGain(totalCapitalGain);
            gainDTO.setBreakup(breakupList);
            gainDTO.setTransactionId(sale.getTransactionId());


            gainDTO.setEventType(sale.getEventType());
            gainDTO.setTradeDate(sale.getTradeDate() != null ? sale.getTradeDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
            gainDTO.setSettlementDate(sale.getSettlementDate() != null ? sale.getSettlementDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
            gainDTO.setQuantity(sale.getQuantity());


            capitalGainResults.add(gainDTO);
        }

        return capitalGainResults;
    }
    /**
     * Helper method to compute and print unique sale keys based on clientCode, securityCode, and settlementDate.
     * Useful for debugging or validation of distinct sale transactions.
     */
    @Override
    public void computeCapitalGains() {
        List<TransactionDTO> allTransactions = mongoService.getAllTransactions();
        List<TransactionDTO> saleTransactions = allTransactions.stream()
                .filter(txn -> "EQ_SAL".equalsIgnoreCase(txn.getEventType()))
                .collect(Collectors.toList());

        Set<String> uniqueSaleKeys = saleTransactions.stream()
                .map(txn -> txn.getClientCode() + "_" + txn.getSecurityCode() + "_" + txn.getSettlementDate())
                .collect(Collectors.toSet());

        System.out.println("Unique sale keys: " + uniqueSaleKeys.size());
        uniqueSaleKeys.forEach(System.out::println);
    }

    /**
     * Groups transaction IDs by their event type (EQ_PUR, EQ_SAL) and returns them as a map.
     * Can be used for reporting or API debugging.
     */

    @Override
    public Map<String, List<String>> getTransactionIdsGroupedByEventType() {
        List<TransactionDTO> allTransactions = mongoService.getAllTransactions();

        return allTransactions.stream()
                .collect(Collectors.groupingBy(
                        TransactionDTO::getEventType,
                        Collectors.mapping(TransactionDTO::getTransactionId, Collectors.toList())
                ));
    }

}