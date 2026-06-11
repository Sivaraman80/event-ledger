package com.ledger.account.controller;

import com.ledger.account.AccountServiceApplication;
import com.ledger.account.dto.TransactionPayload;
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

@SpringBootTest(classes = AccountServiceApplication.class)
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
    void testRunTxAndCalculateBalance_Success() {
        String accountId = "acct-555";

        // Instantiate the record/class matching the target method signature parameter
        TransactionPayload payload = new TransactionPayload(
                "tx-001",
                accountId,
                "CREDIT",
                BigDecimal.valueOf(150.00),
                Instant.now()
        );

        // Call the controller method with the correct DTO payload
        ResponseEntity<Void> runTxResponse = accountController.runTx(accountId, payload);
        assertEquals(HttpStatus.OK, runTxResponse.getStatusCode());

        ResponseEntity<Map<String, Object>> response = accountController.getBalance(accountId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BigDecimal computedBalance = (BigDecimal) response.getBody().get("balance");
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(computedBalance));
    }
}
