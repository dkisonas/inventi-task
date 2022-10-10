package com.inventi.bankstatementsapi.dto;

import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record BankStatementDto(Long id,
                               String accountNumber,
                               LocalDateTime operationDate,
                               String beneficiary,
                               String comment,
                               Double amount,
                               String currency
) {

}
