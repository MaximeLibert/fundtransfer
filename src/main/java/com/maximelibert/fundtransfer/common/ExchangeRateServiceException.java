package com.maximelibert.fundtransfer.common;

import org.springframework.http.HttpStatus;

public class ExchangeRateServiceException extends Exception {
    private final HttpStatus httpStatus;

    public ExchangeRateServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
