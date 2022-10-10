package com.inventi.bankstatementsapi.service;


import com.inventi.bankstatementsapi.csv.BankStatementExportCsvBean;
import com.inventi.bankstatementsapi.csv.BankStatementImportCsvBean;
import com.inventi.bankstatementsapi.dto.AccountBalanceDto;
import com.inventi.bankstatementsapi.dto.BankStatementDto;
import com.inventi.bankstatementsapi.entity.BankStatement;
import com.inventi.bankstatementsapi.mapper.BankStatementMapper;
import com.inventi.bankstatementsapi.repository.BankStatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.inventi.bankstatementsapi.utils.DateUtils.getFrom;
import static com.inventi.bankstatementsapi.utils.DateUtils.getTo;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankStatementService {

    private final CsvService csvService;

    private final BankStatementRepository bankStatementRepository;

    private final BankStatementMapper bankStatementMapper = new BankStatementMapper();

    public List<BankStatementDto> importFromCsv(MultipartFile file) {
        List<BankStatementImportCsvBean> parsedBankStatements =
                csvService.readBeansFromCsv(file, BankStatementImportCsvBean.class);
        List<BankStatement> validBankStatements = getValidBankStatements(parsedBankStatements);
        if (CollectionUtils.isNotEmpty(validBankStatements)) {
            Iterable<BankStatement> savedBankStatements = bankStatementRepository.saveAll(validBankStatements);
            return IterableUtils.toList(savedBankStatements).stream()
                    .map(bankStatementMapper::toDto)
                    .toList();
        }
        return Collections.emptyList();
    }

    public void exportToCsv(LocalDate from, LocalDate to, PrintWriter writer) {
        List<BankStatement> bankStatements = bankStatementRepository.findAllByOperationDateIsBetween(getFrom(from), getTo(to));
        List<BankStatementExportCsvBean> bankStatementExportCsvBeans = bankStatements.stream()
                .map(bankStatementMapper::toExportCsvBean)
                .toList();
        csvService.writeBeansToCsv(bankStatementExportCsvBeans, writer);
    }

    public List<AccountBalanceDto> getAccountBalances(String accountNumber, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = getFrom(from);
        LocalDateTime toDateTime = getTo(to);
        List<BankStatement> sentBankStatements = getSentBankStatements(accountNumber, fromDateTime, toDateTime);
        List<BankStatement> receivedBankStatements = getReceivedBankStatements(accountNumber, fromDateTime, toDateTime);
        List<BankStatement> allBankStatements = Stream.concat(sentBankStatements.stream(), receivedBankStatements.stream())
                .toList();
        return getAccountBalanceDtos(allBankStatements);
    }

    private List<BankStatement> getReceivedBankStatements(String accountNumber, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        return bankStatementRepository.findAllByBeneficiaryAndOperationDateIsBetween(
                accountNumber, fromDateTime, toDateTime);
    }

    private List<BankStatement> getSentBankStatements(String accountNumber, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        return bankStatementRepository.findAllByAccountNumberAndOperationDateIsBetween(
                        accountNumber, fromDateTime, toDateTime)
                .stream()
                .map(bankStatementMapper::toEntityWithNegativeAmount)
                .toList();
    }

    private List<AccountBalanceDto> getAccountBalanceDtos(List<BankStatement> bankStatements) {
        Map<String, Double> amountsMappedByCurrency = getAmountsMappedByCurrency(bankStatements);
        return amountsMappedByCurrency.entrySet().stream()
                .map(entry -> new AccountBalanceDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Map<String, Double> getAmountsMappedByCurrency(List<BankStatement> bankStatements) {
        return bankStatements.stream()
                .map(bankStatement -> Map.entry(bankStatement.getCurrency(), bankStatement.getAmount()))
                .collect(groupingBy(Map.Entry::getKey, summingDouble(Map.Entry::getValue)));
    }

    private List<BankStatement> getValidBankStatements(List<BankStatementImportCsvBean> bankStatementImportCsvBeans) {
        return bankStatementImportCsvBeans.stream()
                .map(bankStatementMapper::toEntity)
                .filter(this::isBankStatementValid)
                .toList();
    }

    private boolean isBankStatementValid(BankStatement bankStatement) {
        if (!bankStatement.isValid()) {
            log.info("Skipping invalid bank statement: {}", bankStatement);
            return false;
        }
        return true;
    }
}
