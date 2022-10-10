package com.inventi.bankstatementsapi.dto;

public record FieldErrorDto(String objectName,
                            String field,
                            String message) {
}
