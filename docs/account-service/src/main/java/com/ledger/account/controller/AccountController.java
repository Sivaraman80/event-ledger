package com.ledger.account.controller;

import com.ledger.account.dto.TransactionPayload;
import com.ledger.account.model.LedgerTx;
import com.ledger.account.repository.TxRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/accounts/{accountId}")
public class AccountController {
    
    private final TxRepository repo;

    public AccountController(TxRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/transactions")
    public ResponseEntity<Void> runTx(@PathVariable("accountId") String accountId, 
                                      @Valid @RequestBody TransactionPayload payload) {
        // Idempotency: skip processing if eventId already recorded down here
        if (repo.existsById(payload.eventId())) {
            return ResponseEntity.ok().build();
        }

        LedgerTx tx = new LedgerTx();
        tx.setEventId(payload.eventId());
        tx.setAccountId(accountId);
        tx.setType(payload.type());
        tx.setAmount(payload.amount());
        tx.setEventTimestamp(payload.eventTimestamp());

        repo.save(tx);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable("accountId") String accountId) {
        BigDecimal balance = repo.calculateBalance(accountId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("accountId", accountId);
        response.put("balance", balance == null ? BigDecimal.ZERO : balance);
        
        return ResponseEntity.ok(response);
    }
}
