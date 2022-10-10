package com.inventi.bankstatementsapi.mapper;

import com.inventi.bankstatementsapi.csv.BankStatementExportCsvBean;
import com.inventi.bankstatementsapi.csv.BankStatementImportCsvBean;
import com.inventi.bankstatementsapi.dto.BankStatementDto;
import com.inventi.bankstatementsapi.entity.BankStatement;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BankStatementMapper {

    public BankStatementExportCsvBean toExportCsvBean(BankStatement bankStatement) {
        return BankStatementExportCsvBean.builder()
                .id(bankStatement.getId())
                .accountNumber(bankStatement.getAccountNumber())
                .operationDate(bankStatement.getOperationDate())
                .beneficiary(bankStatement.getBeneficiary())
                .comment(bankStatement.getComment())
                .amount(bankStatement.getAmount())
                .currency(bankStatement.getCurrency())
                .build();
    }

    public BankStatementDto toDto(BankStatement bankStatement) {
        return BankStatementDto.builder()
                .id(bankStatement.getId())
                .accountNumber(bankStatement.getAccountNumber())
                .operationDate(bankStatement.getOperationDate())
                .beneficiary(bankStatement.getBeneficiary())
                .comment(bankStatement.getComment())
                .amount(bankStatement.getAmount())
                .currency(bankStatement.getCurrency())
                .build();
    }

    public BankStatement toEntity(BankStatementImportCsvBean bankStatementImportCsvBean) {
        return BankStatement.builder()
                .accountNumber(bankStatementImportCsvBean.getAccountNumber())
                .operationDate(bankStatementImportCsvBean.getOperationDate())
                .beneficiary(bankStatementImportCsvBean.getBeneficiary())
                .comment(bankStatementImportCsvBean.getComment())
                .amount(bankStatementImportCsvBean.getAmount())
                .currency(bankStatementImportCsvBean.getCurrency())
                .build();
    }

    public BankStatement toEntityWithNegativeAmount(BankStatement bankStatement) {
        return BankStatement.builder()
                .accountNumber(bankStatement.getAccountNumber())
                .operationDate(bankStatement.getOperationDate())
                .beneficiary(bankStatement.getBeneficiary())
                .comment(bankStatement.getComment())
                .amount(-bankStatement.getAmount())
                .currency(bankStatement.getCurrency())
                .build();
    }
}
