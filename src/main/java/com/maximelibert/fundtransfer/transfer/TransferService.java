package com.maximelibert.fundtransfer.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maximelibert.fundtransfer.account.Account;
import com.maximelibert.fundtransfer.account.AccountService;
import com.maximelibert.fundtransfer.common.ConversionResult;
import com.maximelibert.fundtransfer.common.ExchangeRateService;
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
    public TransferResponseDTO transfer(TransferRequestDTO request) {
        // Validate amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive and not null.");
        }

        // Check if the accounts are the same
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account.");
        }

        // Check accounts exist
        Account fromAccount = accountService.findById(request.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("Source account not found."));
        Account toAccount = accountService.findById(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Destination account not found."));

        // Check sufficient balance
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance.");
        }

        // Get exchange rate and convert amount
        ConversionResult conversionResult = exchangeRateService.convert(fromAccount.getCurrency(),
                toAccount.getCurrency(), request.getAmount());

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(conversionResult.getAmountInTargetCurrency()));

        // Save accounts
        accountService.save(fromAccount);
        accountService.save(toAccount);

        Transfer transfer = Transfer.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .fromCurrency(fromAccount.getCurrency())
                .toCurrency(toAccount.getCurrency())
                .exchangeRate(conversionResult.getExchangeRate())
                .amountInTargetCurrency(conversionResult.getAmountInTargetCurrency())
                .timestamp(LocalDateTime.now())
                .build();
        transferRepository.save(transfer);

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