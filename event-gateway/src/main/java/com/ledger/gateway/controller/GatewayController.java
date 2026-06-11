package com.ledger.gateway.controller;

import com.ledger.gateway.client.AccountClient;
import com.ledger.gateway.dto.EventPayload;
import com.ledger.gateway.model.EventRecord;
import com.ledger.gateway.repository.EventRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GatewayController {
    private final EventRepository repo;
    private final AccountClient client;

    public GatewayController(EventRepository repo, AccountClient client) {
        this.repo = repo;
        this.client = client;
    }

    @PostMapping("/events")
    public ResponseEntity<?> process(@Valid @RequestBody EventPayload payload) {
        if (repo.existsById(payload.eventId())) {
            return ResponseEntity.ok(repo.findById(payload.eventId()).orElseThrow());
        }

        EventRecord rec = new EventRecord();
        rec.setEventId(payload.eventId());
        rec.setAccountId(payload.accountId());
        rec.setType(payload.type());
        rec.setAmount(payload.amount());
        rec.setEventTimestamp(payload.eventTimestamp());
        repo.save(rec);

        client.sendTransaction(payload.accountId(), payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(rec);
    }

    @GetMapping("/events")
    public List<EventRecord> list(@RequestParam("account") String account) {
        return repo.findByAccountIdOrderByEventTimestampAsc(account);
    }
}
