package com.maximelibert.fundtransfer.transfer;

import org.springframework.http.HttpStatus;

public class TransferException extends Exception {
    private final HttpStatus httpStatus;

    public TransferException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
