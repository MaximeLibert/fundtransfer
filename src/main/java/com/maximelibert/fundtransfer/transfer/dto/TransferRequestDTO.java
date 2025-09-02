package com.maximelibert.fundtransfer.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "TransferRequest", description = "Request payload for transferring funds from one account to another. Amount is in the currency of the 'from' account.")
public class TransferRequestDTO {
    @Schema(description = "Account from which the funds are withdrawn")
    @NotNull(message = "From account ID is required")
    @JsonProperty("fromAccountId")
    private Long fromAccountId;

    @Schema(description = "Account that receives the funds")
    @NotNull(message = "To account ID is required")
    @JsonProperty("toAccountId")
    private Long toAccountId;

    @Schema(description = "Amount transferred, in the default currency of the sender account")
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @JsonProperty("amount")
    private BigDecimal amount;
}
