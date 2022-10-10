package com.inventi.bankstatementsapi.repository;

import com.inventi.bankstatementsapi.entity.BankStatement;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BankStatementRepository extends CrudRepository<BankStatement, Long> {
    List<BankStatement> findAllByOperationDateIsBetween(LocalDateTime from, LocalDateTime to);

    List<BankStatement> findAllByAccountNumberAndOperationDateIsBetween(String accountNumber, LocalDateTime from, LocalDateTime to);

    List<BankStatement> findAllByBeneficiaryAndOperationDateIsBetween(String accountNumber, LocalDateTime from, LocalDateTime to);
}
