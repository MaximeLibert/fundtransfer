package com.maximelibert.fundtransfer.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.maximelibert.fundtransfer.account.Account;
import com.maximelibert.fundtransfer.account.AccountRepository;
import com.maximelibert.fundtransfer.common.Currency;
import com.maximelibert.fundtransfer.transfer.dto.TransferRequestDTO;

@SpringBootTest
@ActiveProfiles("test")
public class TransferServiceConcurrencyTest {

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
                .currency(Currency.EUR)
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
    public void testConcurrentTransfers() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        BigDecimal transferAmount = BigDecimal.valueOf(10.25);

        // Submit two transfer tasks
        executor.submit(() -> {
            try {
                startLatch.await();
                TransferRequestDTO request = new TransferRequestDTO(fromAccountId, toAccountId, transferAmount);
                transferService.transfer(request);
            } catch (Exception e) {
                // Ignore exceptions; we'll verify correctness via account balances
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                TransferRequestDTO request = new TransferRequestDTO(fromAccountId, toAccountId, transferAmount);
                transferService.transfer(request);
            } catch (Exception e) {
                // Ignore
            }
        });

        // Start both tasks at the same time
        startLatch.countDown();

        // Shutdown the executor and wait for tasks to complete
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Verify the final balances, make sure operation only happened once
        Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow();
        Account toAccount = accountRepository.findById(toAccountId).orElseThrow();
        assertEquals(BigDecimal.valueOf(989.75).stripTrailingZeros(), fromAccount.getBalance().stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(510.25).stripTrailingZeros(), toAccount.getBalance().stripTrailingZeros());

        executor.shutdown();
    }
}
