package com.inventi.bankstatementsapi.exception;

import com.inventi.bankstatementsapi.constant.ErrorMessages;

public class CsvExportFailedException extends RuntimeException {

    public CsvExportFailedException(Throwable cause) {
        super(ErrorMessages.CSV_EXPORT_FAILED, cause);
    }

}
