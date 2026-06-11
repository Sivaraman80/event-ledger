package com.ledger.gateway.client;

import com.ledger.gateway.dto.EventPayload;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AccountServiceFallback implements AccountClient {
    @Override
    public void sendTransaction(String accountId, EventPayload payload) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Account Service is unavailable.");
    }
}
