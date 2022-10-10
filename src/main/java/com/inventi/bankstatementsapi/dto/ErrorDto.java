package com.inventi.bankstatementsapi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ErrorDto {

    private final String message;

    private List<FieldErrorDto> fieldErrors;

    public ErrorDto(String message) {
        this.message = message;
    }

    public void add(String objectName, String field, String message) {
        if (fieldErrors == null) {
            fieldErrors = new ArrayList<>();
        }
        fieldErrors.add(new FieldErrorDto(objectName, field, message));
    }

}