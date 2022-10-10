package com.inventi.bankstatementsapi.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BankStatementImportCsvBean {

    @CsvBindByName(required = true)
    private String accountNumber;

    @CsvBindByName(required = true)
    @CsvDate("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime operationDate;

    @CsvBindByName(required = true)
    private String beneficiary;

    @CsvBindByName
    private String comment;

    @CsvBindByName(required = true)
    private Double amount;

    @CsvBindByName(required = true)
    private String currency;

}
