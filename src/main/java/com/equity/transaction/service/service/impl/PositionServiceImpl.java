package com.equity.transaction.service.service.impl;

import com.equity.transaction.service.model.PositionDTO;
import com.equity.transaction.service.model.TransactionDTO;
import com.equity.transaction.service.repository.MarketPriceRepository;
import com.equity.transaction.service.repository.PositionRepository;
import com.equity.transaction.service.repository.SubschemaRepository;
import com.equity.transaction.service.repository.TransactionRepository;
import com.equity.transaction.service.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private SubschemaRepository subschemaRepo;

    @Autowired
    private MarketPriceRepository marketPriceRepo;

    @Autowired
    private PositionRepository positionRepo;

    @Override
    public void computePositionMaster() {
        Date lastTxnDateRaw = transactionRepo.findTopByOrderBySettlementDateDesc() != null
                ? transactionRepo.findTopByOrderBySettlementDateDesc().getSettlementDate()
                : null;
        LocalDate lastTxnDate = lastTxnDateRaw != null
                ? lastTxnDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.of(2020, 1, 1);

        LocalDate lastPositionLocalDate = positionRepo.findTopByOrderByDateDesc() != null
                ? positionRepo.findTopByOrderByDateDesc().getDate()
                : null;
        Date lastPositionDateRaw = lastPositionLocalDate != null
                ? Date.from(lastPositionLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                : null;
        LocalDate lastPositionDate = lastPositionDateRaw != null
                ? lastPositionDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.of(2020, 1, 1);

        LocalDate startDate = lastPositionDate.plusDays(1);
        LocalDate endDate = lastTxnDate;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            computeForDate(date);
        }
    }

    private void computeForDate(LocalDate date) {
        List<TransactionDTO> txnList = transactionRepo.findClientSecurityPairsForDate(
                Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        );

        Set<String> uniquePairs = txnList.stream()
                .map(txn -> txn.getClientCode() + "_" + txn.getSecurityCode())
                .collect(Collectors.toSet());

        for (String pair : uniquePairs) {
            Long clientCode = Long.valueOf(pair.split("_")[0]);
            String securityCode = pair.split("_")[1];

            if (positionRepo.existsByClientCodeAndSecurityCodeAndDate(clientCode, securityCode, date)) {
                continue;
            }

            computeAndSavePositionDTO(clientCode, securityCode, date);
        }
    }

    private void computeAndSavePositionDTO(Long clientCode, String securityCode, LocalDate date) {
        Date dateAsDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Integer purchaseQty = transactionRepo.sumQuantityByEventTypeAndClientCodeAndSecurityCodeAndSettlementDateLessThanEqual(
                "EQ_PUR", clientCode, securityCode, dateAsDate);

        Integer saleQty = subschemaRepo.sumQuantityByClientIdAndSecurityCodeAndSaleDateLessThanEqual(
                clientCode, securityCode, dateAsDate);

        int qty = (purchaseQty != null ? purchaseQty : 0) - (saleQty != null ? saleQty : 0);
        if (qty == 0) return;

        Double purchaseAmount = transactionRepo.sumAmountByEventTypeAndClientCodeAndSecurityCodeAndSettlementDateLessThanEqual(
                "EQ_PUR", clientCode, securityCode, dateAsDate);

        Double costOfGoodsSold = subschemaRepo.sumPurchaseValueByClientIdAndSecurityCodeAndSaleDateLessThanEqual(
                clientCode, securityCode, dateAsDate);

        double holdingCost = (purchaseAmount != null ? purchaseAmount : 0.0) - (costOfGoodsSold != null ? costOfGoodsSold : 0.0);
        double avgCost = qty != 0 ? holdingCost / (double) qty : 0.0;

        Double marketPriceToday = marketPriceRepo.findLatestPriceBySecurityCodeAndDate(securityCode, date);
        Double marketPriceTMinus1 = marketPriceRepo.findLatestPriceBySecurityCodeBeforeDate(securityCode, date.minusDays(1));

        double marketValueToday = (marketPriceToday != null ? marketPriceToday : 0.0) * qty;
        double cumGainLoss = marketValueToday - holdingCost;
        double dailyGainLoss = (marketPriceToday != null && marketPriceTMinus1 != null)
                ? (marketPriceToday - marketPriceTMinus1) * qty : 0.0;

        PositionDTO position = new PositionDTO();
        position.setClientCode(clientCode);
        position.setSecurityCode(securityCode);
        position.setDate(date); // PositionDTO uses LocalDate, so set directly
        position.setQty(qty);
        position.setHoldingCost(holdingCost);
        position.setAverageCostPerUnit(avgCost);
        position.setMarketPricePerUnitOnToday(marketPriceToday);
        position.setMarketValueOnToday(marketValueToday);
        position.setCumulativeUnrealisedGainLossUptoToday(cumGainLoss);
        position.setUnrealisedGainLossForToday(dailyGainLoss);
        position.setMarketPricePerUnitT1day(marketPriceTMinus1); // ✅ Set T-1 day price



        positionRepo.save(position);
    }
}
