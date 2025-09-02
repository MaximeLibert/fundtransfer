package com.maximelibert.fundtransfer.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.maximelibert.fundtransfer.account.Account;
import com.maximelibert.fundtransfer.account.AccountRepository;
import com.maximelibert.fundtransfer.common.Currency;
import com.maximelibert.fundtransfer.transfer.dto.TransferRequestDTO;
import com.maximelibert.fundtransfer.transfer.dto.TransferResponseDTO;

@SpringBootTest
@ActiveProfiles("test")
public class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    private Long fromAccountId;
    private Long toAccountId;

    @BeforeEach
    public void setUp() {
        Account fromAccount = Account.builder()
                .ownerId(1001L)
                .currency(Currency.USD)
                .balance(BigDecimal.valueOf(1000))
                .build();
        Account toAccount = Account.builder()
                .ownerId(1002L)
                .currency(Currency.EUR)
                .balance(BigDecimal.valueOf(500))
                .build();

        fromAccount = accountRepository.save(fromAccount);
        toAccount = accountRepository.save(toAccount);

        fromAccountId = fromAccount.getId();
        toAccountId = toAccount.getId();
    }

    @Test
    public void testTransferSuccess() throws Exception {
        // Initial balances
        Account fromAccountBefore = accountRepository.findById(fromAccountId).orElseThrow();
        Account toAccountBefore = accountRepository.findById(toAccountId).orElseThrow();
        BigDecimal initialFromBalance = fromAccountBefore.getBalance();
        BigDecimal initialToBalance = toAccountBefore.getBalance();

        // Perform transfer
        BigDecimal transferAmount = BigDecimal.TEN;
        TransferRequestDTO request = new TransferRequestDTO(fromAccountId, toAccountId, transferAmount);
        TransferResponseDTO response = transferService.transfer(request);

        // Verify the converted amount is reasonable (e.g., not zero or negative)
        BigDecimal expectedConvertedAmount = transferAmount.multiply(response.getExchangeRate()).setScale(2,
                RoundingMode.HALF_UP);

        // Verify response
        assertNotNull(response);
        assertEquals(fromAccountId, response.getFromAccountId());
        assertEquals(toAccountId, response.getToAccountId());
        assertEquals(transferAmount, response.getAmount());
        assertEquals(Currency.USD, response.getFromCurrency());
        assertEquals(Currency.EUR, response.getToCurrency());
        assertEquals(expectedConvertedAmount, response.getAmountInTargetCurrency());

        // Verify updated balances
        Account fromAccountAfter = accountRepository.findById(fromAccountId).orElseThrow();
        Account toAccountAfter = accountRepository.findById(toAccountId).orElseThrow();
        assertEquals(initialFromBalance.subtract(transferAmount), fromAccountAfter.getBalance());
        assertEquals(initialToBalance.add(expectedConvertedAmount), toAccountAfter.getBalance());

    }

    @Test
    public void testTransferInsufficientBalance() {
        TransferRequestDTO request = new TransferRequestDTO(fromAccountId, toAccountId, BigDecimal.valueOf(2000));
        TransferException exception = assertThrows(TransferException.class, () -> transferService.transfer(request));
        assertEquals("Insufficient balance.", exception.getMessage());
    }

    @Test
    public void testTransferSameAccount() {
        TransferRequestDTO request = new TransferRequestDTO(fromAccountId, fromAccountId, BigDecimal.TEN);
        TransferException exception = assertThrows(TransferException.class, () -> transferService.transfer(request));
        assertEquals("Cannot transfer funds to the same account.", exception.getMessage());
    }

    @Test
    public void testTransferNegativeAmount() {
        TransferRequestDTO request = new TransferRequestDTO(fromAccountId, toAccountId, BigDecimal.valueOf(-10));
        TransferException exception = assertThrows(TransferException.class, () -> transferService.transfer(request));
        assertEquals("Transfer amount must be positive and not null.", exception.getMessage());
    }

    @Test
    public void testTransferSourceAccountNotFound() {
        TransferRequestDTO request = new TransferRequestDTO(9999L, toAccountId, BigDecimal.TEN);
        TransferException exception = assertThrows(TransferException.class, () -> transferService.transfer(request));
        assertEquals("Source account not found.", exception.getMessage());
    }

    @Test
    public void testTransferDestinationAccountNotFound() {
        TransferRequestDTO request = new TransferRequestDTO(fromAccountId, 9999L, BigDecimal.TEN);
        TransferException exception = assertThrows(TransferException.class, () -> transferService.transfer(request));
        assertEquals("Destination account not found.", exception.getMessage());
    }
}
