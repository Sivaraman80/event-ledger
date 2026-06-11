package com.ledger.gateway.integration;

import com.ledger.gateway.dto.EventPayload;
import com.ledger.gateway.model.EventRecord;
import com.ledger.gateway.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndToEndLedgerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventRepository eventRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Point the internal client network link to a test port structure 
        registry.add("account-service.url", () -> "http://localhost:8082");
    }

    @BeforeEach
    void cleanSystemStorageState() {
        eventRepository.deleteAll();
    }

    @Test
    void verifyFullGatewayToAccountServiceFlow() {
        String baseUrl = "http://localhost:" + port + "/events";
        EventPayload targetPayload = new EventPayload(
                "e2e-evt-999", "acct-global-1", "CREDIT", BigDecimal.valueOf(450.75), Instant.now()
        );

        // Execute Post Request against the Gateway entry point
        ResponseEntity<EventRecord> response = restTemplate.postForEntity(baseUrl, targetPayload, EventRecord.class);

        // Verify Gateway processed and saved it locally
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("e2e-evt-999", response.getBody().getEventId());
        assertTrue(eventRepository.existsById("e2e-evt-999"));
    }
}
