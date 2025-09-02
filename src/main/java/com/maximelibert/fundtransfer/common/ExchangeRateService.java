package com.maximelibert.fundtransfer.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ExchangeRateService {

    @Value("${openexchangerates.api.url}")
    private String apiUrl;

    @Value("${openexchangerates.app.id}")
    private String appId;

    private final RestTemplate restTemplate = new RestTemplate();

    public ConversionResult convert(Currency fromCurrency, Currency toCurrency, BigDecimal amount)
            throws ExchangeRateServiceException {

        if (fromCurrency == toCurrency) {
            return new ConversionResult(amount, fromCurrency, toCurrency, BigDecimal.ONE, amount, LocalDateTime.now());
        }

        try {
            String url = apiUrl + "?app_id=" + appId;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("rates")) {
                throw new ExchangeRateServiceException(
                        "Invalid response from Open Exchange Rates API: 'rates' not found",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<String, Double> rates = (Map<String, Double>) response.get("rates");

            BigDecimal fromRate = fromCurrency.name().equals("USD")
                    ? BigDecimal.ONE
                    : BigDecimal.valueOf(rates.get(fromCurrency.name()));

            BigDecimal toRate = toCurrency.name().equals("USD")
                    ? BigDecimal.ONE
                    : BigDecimal.valueOf(rates.get(toCurrency.name()));

            BigDecimal conversionRate = toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
            BigDecimal convertedAmount = amount.multiply(conversionRate).setScale(2, RoundingMode.HALF_UP);

            return ConversionResult.builder()
                    .amount(amount)
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .exchangeRate(conversionRate)
                    .amountInTargetCurrency(convertedAmount)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (RestClientException e) {
            // Use a generic message and status for all REST-related exceptions
            throw new ExchangeRateServiceException("Failed to call Open Exchange Rates API: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Fallback for any other unexpected exceptions
            throw new ExchangeRateServiceException("Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
