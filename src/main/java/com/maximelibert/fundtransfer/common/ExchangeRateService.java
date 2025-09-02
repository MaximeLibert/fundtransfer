package com.maximelibert.fundtransfer.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    public ConversionResult convert(Currency fromCurrency, Currency toCurrency, BigDecimal amount) {
        if (fromCurrency == toCurrency) {
            return new ConversionResult(amount, fromCurrency, toCurrency, BigDecimal.ONE, amount, LocalDateTime.now());
        }

        String url = apiUrl + "?app_id=" + appId;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        Map<String, Double> rates = (Map<String, Double>) response.get("rates");

        // Compute exchange rate through USD cause Free api only works with base curency
        // "USD" also API returns Double for rate
        BigDecimal fromRate = fromCurrency.name().equals("USD") ? BigDecimal.ONE
                : BigDecimal.valueOf(rates.get(fromCurrency.name()));
        BigDecimal toRate = toCurrency.name().equals("USD") ? BigDecimal.ONE
                : BigDecimal.valueOf(rates.get(toCurrency.name()));

        BigDecimal conversionRate = toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amount.multiply(conversionRate).setScale(2, RoundingMode.HALF_UP);

        return ConversionResult.builder().amount(amount).fromCurrency(fromCurrency).toCurrency(toCurrency)
                .exchangeRate(conversionRate).amountInTargetCurrency(convertedAmount).timestamp(LocalDateTime.now())
                .build();
    }
}