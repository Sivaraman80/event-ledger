package com.ledger.gateway.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;

public record EventPayload(
    @NotBlank(message = "eventId is required") 
    String eventId,
    
    @NotBlank(message = "accountId is required") 
    String accountId,
    
    @Pattern(regexp = "^(CREDIT|DEBIT)$", message = "Type must be CREDIT or DEBIT") 
    String type,
    
    @NotNull(message = "amount is required") 
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0") 
    BigDecimal amount,
    
    @NotNull(message = "eventTimestamp is required") 
    Instant eventTimestamp
) {}
