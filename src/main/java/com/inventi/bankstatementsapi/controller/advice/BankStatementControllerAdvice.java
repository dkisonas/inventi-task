package com.inventi.bankstatementsapi.controller.advice;

import com.inventi.bankstatementsapi.constant.ErrorMessages;
import com.inventi.bankstatementsapi.dto.ErrorDto;
import com.inventi.bankstatementsapi.exception.CsvExportFailedException;
import com.inventi.bankstatementsapi.exception.CsvImportFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class BankStatementControllerAdvice {


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorDto dto = new ErrorDto(ErrorMessages.METHOD_ARGUMENT_TYPE_MISMATCH_ERROR);
        dto.add(e.getName(), e.getParameter().getParameterName(), e.getMessage());
        return dto;
    }

    @ExceptionHandler(CsvImportFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleCsvImportFailedException(CsvImportFailedException e) {
        return e.getMessage();
    }

    @ExceptionHandler(CsvExportFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleCsvExportFailedException(CsvImportFailedException e) {
        return e.getMessage();
    }

}
