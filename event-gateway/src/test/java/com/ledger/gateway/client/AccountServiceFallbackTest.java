package com.ledger.gateway.client;

import com.ledger.gateway.dto.EventPayload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountServiceFallbackTest {

    @Autowired
    private AccountServiceFallback fallbackEngine;

    @Test
    void testCircuitBreakerGracefulDegradationException() {
        EventPayload payload = new EventPayload(
                "evt-fail", "acct-000", "DEBIT", BigDecimal.ONE, Instant.now()
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            fallbackEngine.sendTransaction("acct-000", payload);
        });

        assertEquals(503, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Account Service is unavailable."));
    }
}
