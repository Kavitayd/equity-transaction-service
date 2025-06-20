package com.equity.transaction.service.controller;

import com.equity.transaction.service.model.CapitalGainDTO;
import com.equity.transaction.service.service.CapitalGainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/capital-gains")
public class CapitalGainController {

    @Autowired
    private CapitalGainService capitalGainService;

    @GetMapping("/compute")
    public ResponseEntity<String> computeGains() {
        capitalGainService.computeCapitalGains();
        return ResponseEntity.ok("Capital gain computation triggered.");
    }
    @GetMapping("/grouped-by-event-type")
    public ResponseEntity<Map<String, List<String>>> getGroupedTransactionIds() {
        Map<String, List<String>> grouped = capitalGainService.getTransactionIdsGroupedByEventType();
        return ResponseEntity.ok(grouped);
    }
}
