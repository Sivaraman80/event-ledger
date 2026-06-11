package com.ledger.account.controller;

import com.ledger.account.dto.TransactionPayload;
import com.ledger.account.model.LedgerTx;
import com.ledger.account.repository.TxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountControllerTest {

    @Autowired
    private TxRepository txRepository;

    @Autowired
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        txRepository.deleteAll();
    }

    @Test
    void testRunTxAndCalculateBalance_OutofOrderSafe() {
        String accountId = "acct-555";
        Instant now = Instant.now();

        // 1. Credit Transaction arriving late chronologically
        TransactionPayload tx1 = new TransactionPayload(
                "tx-001", accountId, "CREDIT", BigDecimal.valueOf(150.00), now.plusSeconds(100)
        );
        // 2. Debit Transaction arriving early chronologically
        TransactionPayload tx2 = new TransactionPayload(
                "tx-002", accountId, "DEBIT", BigDecimal.valueOf(50.00), now
        );

        accountController.runTx(accountId, tx1);
        accountController.runTx(accountId, tx2);

        // Fetch running balances
        ResponseEntity<Map<String, Object>> response = accountController.getBalance(accountId);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        BigDecimal computedBalance = (BigDecimal) response.getBody().get("balance");
        // 150.00 - 50.00 = 100.00
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(computedBalance), 
                "Balance aggregation logic must handle out-of-order sequence inputs cleanly.");
    }

    @Test
    void testDuplicateTransactionDownstreamIdempotency() {
        String accountId = "acct-555";
        TransactionPayload tx = new TransactionPayload(
                "tx-unique", accountId, "CREDIT", BigDecimal.valueOf(10.00), Instant.now()
        );

        // First application
        ResponseEntity<Void> res1 = accountController.runTx(accountId, tx);
        // Duplicate application
        ResponseEntity<Void> res2 = accountController.runTx(accountId, tx);

        assertEquals(HttpStatus.OK, res1.getStatusCode());
        assertEquals(HttpStatus.OK, res2.getStatusCode());
        
        // Assert balance is only changed once ($10.00 instead of $20.00)
        BigDecimal balance = txRepository.calculateBalance(accountId);
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(balance));
    }
}
