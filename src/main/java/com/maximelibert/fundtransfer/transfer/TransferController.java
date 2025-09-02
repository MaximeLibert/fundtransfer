package com.maximelibert.fundtransfer.transfer;

import com.maximelibert.fundtransfer.transfer.dto.TransferRequestDTO;
import com.maximelibert.fundtransfer.transfer.dto.TransferResponseDTO;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
@AllArgsConstructor
@Tag(name = "Transfers")
public class TransferController {
    private final TransferService transferService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponseDTO> transferFunds(
            @RequestBody(required = true) @Valid TransferRequestDTO transfer) {
        return ResponseEntity.ok(transferService.transfer(transfer));
    }
}
