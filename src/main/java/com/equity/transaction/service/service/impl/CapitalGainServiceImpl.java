package com.equity.transaction.service.service.impl;

import com.equity.transaction.service.model.CapitalGainBreakupDTO;
import com.equity.transaction.service.model.CapitalGainDTO;
import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.repository.TransactionRepository;
import com.equity.transaction.service.service.CapitalGainService;
import com.equity.transaction.service.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
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

                Date purchaseDate = purchase.getSettlementDate();
                Date saleDate = sale.getSettlementDate();

                long holdingDays = ChronoUnit.DAYS.between(
                        purchaseDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        saleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                );

                String gainType = holdingDays > 365 ? "Long Term Capital Gain" : "Short Term Capital Gain";
                double gain = (sale.getRate() - purchase.getRate()) * matchedQty;

                CapitalGainBreakupDTO breakup = new CapitalGainBreakupDTO();
                breakup.setPurchaseTransactionId(purchase.getTransactionId());
                breakup.setSaleTransactionId(sale.getTransactionId());

                breakup.setClientCode(sale.getClientCode());
                breakup.setClientName(sale.getClientName());
                breakup.setSecurityCode(sale.getSecurityCode());
                breakup.setSecurityName(sale.getSecurityName());
                breakup.setIsin(sale.getIsin());
                breakup.setListingStatus(sale.getListingStatus());
                breakup.setPrdHoldingFlag(sale.getPrdHoldingFlag());

                breakup.setPurchaseDate(purchaseDate);
                breakup.setSaleDate(saleDate);
                breakup.setCapitalGainType(gainType);
                breakup.setPurchasePrice(purchase.getRate());
                breakup.setSalePrice(sale.getRate());
                breakup.setQuantity(matchedQty);
                breakup.setGainOrLoss(gain);
                breakup.setHoldingPeriodDays((int) holdingDays);
                breakup.setPurchaseValue(purchase.getRate() * matchedQty);
                breakup.setSaleValue(sale.getRate() * matchedQty);

                purchase.setAvailableBuyQuantity(purchase.getAvailableBuyQuantity() - matchedQty);
                saleQtyRemaining -= matchedQty;

                breakup.setBalancePurchaseQty(purchase.getAvailableBuyQuantity());
                breakup.setBalanceSaleQty(saleQtyRemaining);

                breakupList.add(breakup);
                totalCapitalGain += gain;

                if (saleQtyRemaining <= 0) break;
            }

            CapitalGainDTO gainDTO = new CapitalGainDTO();
            gainDTO.setClientCode(sale.getClientCode());
            gainDTO.setSecurityCode(sale.getSecurityCode());
            gainDTO.setTotalCapitalGain(totalCapitalGain);
            gainDTO.setBreakup(breakupList);
            gainDTO.setTransactionId(sale.getTransactionId());
            gainDTO.setEventType(sale.getEventType());
            gainDTO.setQuantity(sale.getQuantity());

            //gainDTO.setTradeDate(sale.getTradeDate());
            //gainDTO.setSettlementDate(sale.getSettlementDate());

            gainDTO.setTradeDate(toLocalDate(sale.getTradeDate()));
            gainDTO.setSettlementDate(toLocalDate(sale.getSettlementDate()));


            capitalGainResults.add(gainDTO);
        }

        return capitalGainResults;
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
