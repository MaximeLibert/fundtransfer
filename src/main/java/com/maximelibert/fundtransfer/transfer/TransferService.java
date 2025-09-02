package com.maximelibert.fundtransfer.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.OptimisticLockException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maximelibert.fundtransfer.account.Account;
import com.maximelibert.fundtransfer.account.AccountService;
import com.maximelibert.fundtransfer.common.ConversionResult;
import com.maximelibert.fundtransfer.common.ExchangeRateService;
import com.maximelibert.fundtransfer.common.ExchangeRateServiceException;
import com.maximelibert.fundtransfer.transfer.dto.TransferRequestDTO;
import com.maximelibert.fundtransfer.transfer.dto.TransferResponseDTO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TransferService {

    private final AccountService accountService;
    private final TransferRepository transferRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO request)
            throws ExchangeRateServiceException, TransferException, OptimisticLockException {

        // Validate amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferException("Transfer amount must be positive and not null.", HttpStatus.BAD_REQUEST);
        }

        // Check if the accounts are not the same
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new TransferException("Cannot transfer funds to the same account.", HttpStatus.BAD_REQUEST);
        }

        // Check accounts exist
        Account fromAccount = accountService.findById(request.getFromAccountId())
                .orElseThrow(() -> new TransferException("Source account not found.", HttpStatus.NOT_FOUND));

        Account toAccount = accountService.findById(request.getToAccountId())
                .orElseThrow(() -> new TransferException("Destination account not found.", HttpStatus.NOT_FOUND));

        // Check sufficient balance
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new TransferException("Insufficient balance.", HttpStatus.BAD_REQUEST);
        }

        // Get exchange rate and convert amount
        ConversionResult conversionResult = exchangeRateService.convert(
                fromAccount.getCurrency(),
                toAccount.getCurrency(),
                request.getAmount());

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(conversionResult.getAmountInTargetCurrency()));

        // Save accounts (will trigger version check)
        accountService.save(fromAccount);
        accountService.save(toAccount);

        // Save transfer
        Transfer transfer = Transfer.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .fromCurrency(fromAccount.getCurrency())
                .toCurrency(toAccount.getCurrency())
                .exchangeRate(conversionResult.getExchangeRate())
                .amountInTargetCurrency(conversionResult.getAmountInTargetCurrency())
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        transferRepository.save(transfer);

        // Return response
        return TransferResponseDTO.builder()
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(request.getAmount())
                .fromCurrency(fromAccount.getCurrency())
                .toCurrency(toAccount.getCurrency())
                .exchangeRate(conversionResult.getExchangeRate())
                .amountInTargetCurrency(conversionResult.getAmountInTargetCurrency())
                .timestamp(transfer.getTimestamp())
                .build();

    }
}
