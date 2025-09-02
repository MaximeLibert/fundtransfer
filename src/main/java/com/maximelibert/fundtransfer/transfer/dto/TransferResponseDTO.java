package com.maximelibert.fundtransfer.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.maximelibert.fundtransfer.common.Currency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "TransfertResponse", description = "Response payload for transfering funds from one account to another.")
public class TransferResponseDTO {

    @Schema(description = "Account ID from which the funds are withdawn")
    private Long fromAccountId;

    @Schema(description = "Account ID to which the funds are transfered")
    private Long toAccountId;

    @Schema(description = "The amount that was send")
    private BigDecimal amount;

    @Schema(description = "The curency of the account from where the funds are transfered")
    @Enumerated(EnumType.STRING)
    private Currency fromCurrency;

    @Schema(description = "The curency of the account to which the funds are transfered")
    @Enumerated(EnumType.STRING)
    private Currency toCurrency;

    @Schema(description = "The exchange rate")
    private BigDecimal exchangeRate;

    @Schema(description = "The amount that was send in the recipient currency")
    private BigDecimal amountInTargetCurrency;

    @Schema(description = "The imestamp of the transaction")
    private LocalDateTime timestamp;
}
