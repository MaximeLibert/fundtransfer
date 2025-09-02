package com.maximelibert.fundtransfer.common;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ConversionResult {
    private final BigDecimal amount;
    private final Currency fromCurrency;
    private final Currency toCurrency;
    private final BigDecimal exchangeRate;
    private final BigDecimal amountInTargetCurrency;
    private final LocalDateTime timestamp;
}