package com.ledger.gateway.controller;

import com.ledger.gateway.EventGatewayApplication;
import com.ledger.gateway.client.AccountClient;
import com.ledger.gateway.dto.EventPayload;
import com.ledger.gateway.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = EventGatewayApplication.class)
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

        Mockito.doNothing().when(accountClient).sendTransaction(any(), any());

        ResponseEntity<?> response = gatewayController.process(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(eventRepository.existsById("evt-101"));
    }
}
