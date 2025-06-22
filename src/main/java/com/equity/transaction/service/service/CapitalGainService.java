package com.equity.transaction.service.service;

import com.equity.transaction.service.model.CapitalGainDTO;

import java.util.List;
import java.util.Map;

public interface CapitalGainService {
    void computeCapitalGains();
    List<CapitalGainDTO> computeCapitalGainsUsingFIFO();
    Map<String, List<String>> getTransactionIdsGroupedByEventType();
}

