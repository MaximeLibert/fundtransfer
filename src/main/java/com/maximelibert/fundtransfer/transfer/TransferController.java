package com.maximelibert.fundtransfer.transfer;

import com.maximelibert.fundtransfer.common.ExchangeRateServiceException;
import com.maximelibert.fundtransfer.transfer.dto.TransferRequestDTO;
import com.maximelibert.fundtransfer.transfer.dto.TransferResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/transfers")
@AllArgsConstructor
@Tag(name = "Transfers")
public class TransferController {
    private final TransferService transferService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponseDTO> transferFunds(
            @RequestBody(required = true) @Valid TransferRequestDTO request)
            throws ExchangeRateServiceException, TransferException {
        try {
            TransferResponseDTO response = transferService.transfer(request);
            return ResponseEntity.ok(response);
        } catch (TransferException | ExchangeRateServiceException e) {
            throw e;
        }
    }

    @ExceptionHandler(TransferException.class)
    public ResponseEntity<Map<String, String>> handleTransferException(TransferException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ExchangeRateServiceException.class)
    public ResponseEntity<Map<String, String>> handleExchangeRateServiceException(ExchangeRateServiceException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred."));
    }
}
