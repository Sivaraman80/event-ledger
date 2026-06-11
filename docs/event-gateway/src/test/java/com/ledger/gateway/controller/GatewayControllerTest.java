package com.ledger.gateway.controller;

import com.ledger.gateway.client.AccountClient;
import com.ledger.gateway.dto.EventPayload;
import com.ledger.gateway.model.EventRecord;
import com.ledger.gateway.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
public class GatewayControllerTest {

    @Autowired
    private EventRepository eventRepository;

    @MockBean
    private AccountClient accountClient;

    @Autowired
    private GatewayController gatewayController;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void testProcessNewEvent_Success() {
        EventPayload payload = new EventPayload(
                "evt-101", "acct-999", "CREDIT", BigDecimal.valueOf(250.00), Instant.now()
        );

        ResponseEntity<?> response = gatewayController.process(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(eventRepository.existsById("evt-101"));
        Mockito.verify(accountClient, Mockito.times(1)).sendTransaction(eq("acct-999"), any(EventPayload.class));
    }

    @Test
    void testProcessDuplicateEvent_IdempotencyGaurded() {
        // Pre-populate database with an existing record
        EventRecord existing = new EventRecord();
        existing.setEventId("evt-101");
        existing.setAccountId("acct-999");
        existing.setType("CREDIT");
        existing.setAmount(BigDecimal.valueOf(250.00));
        existing.setEventTimestamp(Instant.now());
        eventRepository.save(existing);

        EventPayload payload = new EventPayload(
                "evt-101", "acct-999", "CREDIT", BigDecimal.valueOf(250.00), Instant.now()
        );

        ResponseEntity<?> response = gatewayController.process(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Verify client was NOT called again on duplication detection
        Mockito.verify(accountClient, Mockito.never()).sendTransaction(any(), any());
    }

    @Test
    void testChronologicalSortingForAccountEvents() {
        Instant now = Instant.now();
        
        // Late arrival inserted first
        EventRecord record1 = new EventRecord();
        record1.setEventId("evt-1"); record1.setAccountId("acct-123"); record1.setType("CREDIT");
        record1.setAmount(BigDecimal.TEN); record1.setEventTimestamp(now.plusSeconds(60));
        
        // Early event inserted second
        EventRecord record2 = new EventRecord();
        record2.setEventId("evt-2"); record2.setAccountId("acct-123"); record2.setType("CREDIT");
        record2.setAmount(BigDecimal.ONE); record2.setEventTimestamp(now);

        eventRepository.saveAll(List.of(record1, record2));

        List<EventRecord> result = gatewayController.list("acct-123");

        assertEquals(2, result.size());
        assertEquals("evt-2", result.get(0).getEventId(), "Earliest timestamp should be first.");
        assertEquals("evt-1", result.get(1).getEventId(), "Latest timestamp should be last.");
    }
}
