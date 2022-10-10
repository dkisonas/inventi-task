package com.inventi.bankstatementsapi.exception;

public class CsvImportFailedException extends RuntimeException {

    public CsvImportFailedException(String message) {
        super(message);
    }

    public CsvImportFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
