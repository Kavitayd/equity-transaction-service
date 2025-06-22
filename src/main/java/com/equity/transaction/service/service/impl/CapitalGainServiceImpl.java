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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class CapitalGainServiceImpl implements CapitalGainService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MongoService mongoService;
    @Override
    public List<CapitalGainDTO> computeCapitalGainsUsingFIFO() {
        List<TransactionDTO> allTransactions = mongoService.getAllTransactions();

        List<TransactionDTO> purchaseTransactions = allTransactions.stream()
                .filter(txn -> "EQ_PUR".equalsIgnoreCase(txn.getEventType()))
                .filter(txn -> txn.getSettlementDate() != null)
                .sorted(Comparator.comparing(TransactionDTO::getSettlementDate))
                .collect(Collectors.toList());

        List<TransactionDTO> saleTransactions = allTransactions.stream()
                .filter(txn -> "EQ_SAL".equalsIgnoreCase(txn.getEventType()))
                .filter(txn -> txn.getSettlementDate() != null)
                .sorted(Comparator.comparing(TransactionDTO::getSettlementDate))
                .collect(Collectors.toList());

        List<CapitalGainDTO> capitalGainResults = new ArrayList<>();

        for (TransactionDTO sale : saleTransactions) {
            double saleQtyRemaining = sale.getQuantity();
            List<CapitalGainBreakupDTO> breakupList = new ArrayList<>();
            double totalCapitalGain = 0;

            for (TransactionDTO purchase : purchaseTransactions) {
                if (!Objects.equals(sale.getClientCode(), purchase.getClientCode()) ||
                        !Objects.equals(sale.getSecurityCode(), purchase.getSecurityCode())) {
                    continue;
                }

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

                CapitalGainBreakupDTO breakup = new CapitalGainBreakupDTO();
                breakup.setPurchaseTransactionId(purchase.getTransactionId());
                breakup.setSaleTransactionId(sale.getTransactionId());
                breakup.setPurchaseDate(purchase.getSettlementDate());
                breakup.setSaleDate(sale.getSettlementDate());
                breakup.setPurchaseRate(purchase.getRate().doubleValue());
                breakup.setSaleRate(sale.getRate().doubleValue());
                breakup.setQuantity(matchedQty);
                breakup.setGainOrLoss(gain);
                breakup.setHoldingPeriodDays((int) holdingDays);
                breakup.setCapitalGainType(gainType);

                breakupList.add(breakup);
                totalCapitalGain += gain;

                // Reduce available quantity
                purchase.setAvailableBuyQuantity(purchase.getAvailableBuyQuantity() - matchedQty);
                saleQtyRemaining -= matchedQty;

                if (saleQtyRemaining <= 0) break;
            }

            CapitalGainDTO gainDTO = new CapitalGainDTO();
            gainDTO.setClientCode(sale.getClientCode());
            gainDTO.setSecurityCode(sale.getSecurityCode());
            gainDTO.setSaleDate(sale.getSettlementDate());
            gainDTO.setTotalCapitalGain(totalCapitalGain);
            gainDTO.setBreakup(breakupList);
            gainDTO.setTransactionId(sale.getTransactionId());

            capitalGainResults.add(gainDTO);
        }

        return capitalGainResults;
    }

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