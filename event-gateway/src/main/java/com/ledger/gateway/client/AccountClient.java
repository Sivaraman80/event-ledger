package com.ledger.gateway.client;

import com.ledger.gateway.dto.EventPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", url = "http://localhost:8082", fallback = AccountServiceFallback.class)
public interface AccountClient {
    @PostMapping("/accounts/{accountId}/transactions")
    void sendTransaction(@PathVariable("accountId") String accountId, @RequestBody EventPayload payload);
}
