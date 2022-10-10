package com.inventi.bankstatementsapi;

import com.inventi.bankstatementsapi.csv.BankStatementExportCsvBean;
import com.inventi.bankstatementsapi.csv.BankStatementImportCsvBean;
import com.inventi.bankstatementsapi.dto.AccountBalanceDto;
import com.inventi.bankstatementsapi.dto.BankStatementDto;
import com.inventi.bankstatementsapi.entity.BankStatement;
import com.inventi.bankstatementsapi.mapper.BankStatementMapper;
import com.inventi.bankstatementsapi.repository.BankStatementRepository;
import com.inventi.bankstatementsapi.service.BankStatementService;
import com.inventi.bankstatementsapi.service.CsvService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankStatementServiceTest {

    private static final String CSV_CONTENT_TYPE = "text/csv";
    public static final LocalDateTime DATE_TIME_FROM = LocalDateTime.of(2022, 1, 1, 0, 0);
    public static final LocalDateTime DATE_TIME_TO = LocalDateTime.of(2022, 12, 31, 0, 0);
    public static final LocalDate DATE_FROM = LocalDate.of(2022, 1, 1);
    public static final LocalDate DATE_TO = LocalDate.of(2022, 12, 31);
    private final BankStatementMapper bankStatementMapper = new BankStatementMapper();

    @Mock
    private BankStatementRepository bankStatementRepository;

    @Mock
    private CsvService csvService;

    private final MultipartFile multipartFile = mock(MultipartFile.class);

    @InjectMocks
    private BankStatementService bankStatementService;


    @Test
    void importFromCsv_whenFileContainsCsvData_shouldReturnValidBankStatements() throws IOException {
        byte[] bytes = {0};
        List<BankStatementImportCsvBean> parsedBankStatements = List.of(
                new BankStatementImportCsvBean("account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 500D, "EUR"),
                new BankStatementImportCsvBean(null, parseLocalDateTime("2022-09-20T20:55:41"), "account3", StringUtils.EMPTY, 200D, "EUR"),
                new BankStatementImportCsvBean("account2", parseLocalDateTime("2022-09-21T20:55:41"), null, StringUtils.EMPTY, 300D, "EUR"),
                new BankStatementImportCsvBean("account3", parseLocalDateTime("2022-09-22T20:55:41"), "account4", StringUtils.EMPTY, 300D, "USD")
        );
        List<BankStatement> validBankStatements = parsedBankStatements.stream()
                .map(bankStatementMapper::toEntity)
                .filter(BankStatement::isValid)
                .toList();
        when(csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class)).thenReturn(parsedBankStatements);
        when(bankStatementRepository.saveAll(validBankStatements)).thenReturn(validBankStatements);
        mockCsvFile(bytes);
        List<BankStatementDto> bankStatementDtos = bankStatementService.importFromCsv(multipartFile);
        assertEquals(2, bankStatementDtos.size());
        assertEquals(parsedBankStatements.get(0).getAccountNumber(), bankStatementDtos.get(0).accountNumber());
        assertEquals(parsedBankStatements.get(3).getAmount(), bankStatementDtos.get(1).amount());
    }

    @Test
    void importFromCsv_whenFileContainsOnlyHeader_shouldReturnEmptyList() throws IOException {
        byte[] bytes = {0};
        mockCsvFile(bytes);
        when(csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class)).thenReturn(Collections.emptyList());
        List<BankStatementDto> bankStatementDtos = bankStatementService.importFromCsv(multipartFile);
        assertEquals(0, bankStatementDtos.size());
    }

    @Test
    void exportToCsv_whenBankStatementsExist_shouldWriteToFile() {
        List<BankStatement> bankStatements = List.of(
                new BankStatement(1L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 100D, "EUR"),
                new BankStatement(2L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 100D, "EUR")
        );
        when(bankStatementRepository.findAllByOperationDateIsBetween(any(), any())).thenReturn(bankStatements);
        List<BankStatementExportCsvBean> bankStatementExportCsvBeans = bankStatements.stream()
                .map(bankStatementMapper::toExportCsvBean)
                .toList();
        PrintWriter writer = mock(PrintWriter.class);
        bankStatementService.exportToCsv(DATE_FROM, DATE_TO, writer);
        verify(csvService).writeBeansToCsv(bankStatementExportCsvBeans, writer);
    }

    @Test
    void exportToCsv_whenBankStatementsAreEmpty_shouldWriteEmptyFile() {
        List<BankStatement> bankStatements = new ArrayList<>();
        when(bankStatementRepository.findAllByOperationDateIsBetween(any(), any())).thenReturn(bankStatements);
        List<BankStatementExportCsvBean> emptyBankStatementExportCsvBeans = bankStatements.stream()
                .map(bankStatementMapper::toExportCsvBean)
                .toList();
        PrintWriter writer = mock(PrintWriter.class);
        bankStatementService.exportToCsv(DATE_FROM, DATE_TO, writer);
        verify(csvService).writeBeansToCsv(emptyBankStatementExportCsvBeans, writer);
    }

    @Nested
    @DisplayName("Account balance with single currency")
    class SingleCurrencyBalance {

        @Test
        void getAccountBalance_whenAccountHasNoBankStatements_shouldReturnEmptyList() {
            when(bankStatementRepository.findAllByAccountNumberAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(Collections.emptyList());
            when(bankStatementRepository.findAllByBeneficiaryAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(Collections.emptyList());
            List<AccountBalanceDto> accountBalances = bankStatementService.getAccountBalances("account1", DATE_FROM, DATE_TO);
            assertEquals(0, accountBalances.size());
        }

        @Test
        void getAccountBalance_whenAccountHasReceivedBankStatementsNoSentBankStatements_shouldReturnListOfBalances() {
            when(bankStatementRepository.findAllByAccountNumberAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(Collections.emptyList());
            when(bankStatementRepository.findAllByBeneficiaryAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO)).thenReturn(
                    List.of(
                            new BankStatement(1L, "account2", parseLocalDateTime("2022-09-19T20:55:41"), "account1", "comment", 100D, "EUR"),
                            new BankStatement(2L, "account2", parseLocalDateTime("2022-09-20T20:55:41"), "account1", "comment", 100D, "EUR")
                    )
            );
            List<AccountBalanceDto> accountBalances = bankStatementService.getAccountBalances("account1", DATE_FROM, DATE_TO);
            assertEquals(1, accountBalances.size());
            assertEquals(200D, accountBalances.get(0).amount());
            assertEquals("EUR", accountBalances.get(0).currency());
        }

        @Test
        void getAccountBalance_whenAccountHasSentBankStatementsAndNoReceivedBankStatements_shouldReturnListOfNegativeBalances() {
            when(bankStatementRepository.findAllByAccountNumberAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO)).thenReturn(
                    List.of(
                            new BankStatement(1L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 100D, "EUR"),
                            new BankStatement(2L, "account1", parseLocalDateTime("2022-09-20T20:55:41"), "account2", "comment", 100D, "EUR")
                    )
            );
            when(bankStatementRepository.findAllByBeneficiaryAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(Collections.emptyList());
            List<AccountBalanceDto> accountBalances = bankStatementService.getAccountBalances("account1", DATE_FROM, DATE_TO);
            assertEquals(1, accountBalances.size());
            assertEquals(-200D, accountBalances.get(0).amount());
            assertEquals("EUR", accountBalances.get(0).currency());
        }

        @Test
        void getAccountBalance_whenAccountHasSentBankStatementsAndReceivedBankStatements_shouldReturnListOfBalances() {
            when(bankStatementRepository.findAllByAccountNumberAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO)).thenReturn(
                    List.of(
                            new BankStatement(1L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 100D, "EUR"),
                            new BankStatement(2L, "account1", parseLocalDateTime("2022-09-20T20:55:41"), "account2", "comment", 100D, "EUR")
                    )
            );
            when(bankStatementRepository.findAllByBeneficiaryAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO)).thenReturn(
                    List.of(
                            new BankStatement(3L, "account2", parseLocalDateTime("2022-09-21T20:55:41"), "account1", "comment", 200D, "EUR"),
                            new BankStatement(4L, "account2", parseLocalDateTime("2022-09-22T20:55:41"), "account1", "comment", 200D, "EUR")
                    )
            );
            List<AccountBalanceDto> accountBalances = bankStatementService.getAccountBalances("account1", DATE_FROM, DATE_TO);
            assertEquals(1, accountBalances.size());
            assertEquals(200D, accountBalances.get(0).amount());
            assertEquals("EUR", accountBalances.get(0).currency());
        }

    }

    @Nested
    @DisplayName("Account balance with multiple currencies")
    class MultipleCurrencyBalance {

        @Test
        void getAccountBalance_whenAccountHasSentBankStatementsWithDifferentCurrenciesAndNoReceivedBankStatements_shouldReturnListOfNegativeBalancesGroupedByCurrency() {
            when(bankStatementRepository.findAllByAccountNumberAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(List.of(
                                    new BankStatement(1L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 100D, "USD"),
                                    new BankStatement(2L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 100D, "EUR")
                            )
                    );
            when(bankStatementRepository.findAllByBeneficiaryAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO)).thenReturn(
                    Collections.emptyList()
            );
            List<AccountBalanceDto> accountBalances = bankStatementService.getAccountBalances("account1", DATE_FROM, DATE_TO);
            assertEquals(2, accountBalances.size());
            assertEquals(-100D, accountBalances.get(0).amount());
            assertEquals("EUR", accountBalances.get(0).currency());
            assertEquals(-100D, accountBalances.get(1).amount());
            assertEquals("USD", accountBalances.get(1).currency());
        }

        @Test
        void getAccountBalance_whenAccountHasReceivedBankStatementsWithDifferentCurrenciesAndNoSentBankStatements_shouldReturnListOfBalancesGroupedByCurrency() {
            when(bankStatementRepository.findAllByAccountNumberAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(Collections.emptyList());
            when(bankStatementRepository.findAllByBeneficiaryAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(List.of(
                            new BankStatement(1L, "account2", parseLocalDateTime("2022-09-19T20:55:41"), "account1", "comment", 100D, "USD"),
                            new BankStatement(2L, "account2", parseLocalDateTime("2022-09-19T20:55:41"), "account1", "comment", 100D, "EUR")
                    ));
            List<AccountBalanceDto> accountBalances = bankStatementService.getAccountBalances("account1", DATE_FROM, DATE_TO);
            assertEquals(2, accountBalances.size());
            assertEquals(100D, accountBalances.get(0).amount());
            assertEquals("EUR", accountBalances.get(0).currency());
            assertEquals(100D, accountBalances.get(1).amount());
            assertEquals("USD", accountBalances.get(1).currency());
        }

        @Test
        void getAccountBalance_whenAccountHasReceivedBankStatementsAndSentBankStatementsWithDifferentCurrencies_shouldReturnListOfBalancesGroupedByCurrency() {
            when(bankStatementRepository.findAllByAccountNumberAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(List.of(
                            new BankStatement(1L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 100D, "USD"),
                            new BankStatement(2L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 100D, "EUR")
                    ));
            when(bankStatementRepository.findAllByBeneficiaryAndOperationDateIsBetween("account1", DATE_TIME_FROM, DATE_TIME_TO))
                    .thenReturn(List.of(
                            new BankStatement(1L, "account2", parseLocalDateTime("2022-09-19T20:55:41"), "account1", "comment", 200D, "USD"),
                            new BankStatement(2L, "account2", parseLocalDateTime("2022-09-19T20:55:41"), "account1", "comment", 200D, "EUR")
                    ));
            List<AccountBalanceDto> accountBalances = bankStatementService.getAccountBalances("account1", DATE_FROM, DATE_TO);
            assertEquals(2, accountBalances.size());
            assertEquals(100D, accountBalances.get(0).amount());
            assertEquals("EUR", accountBalances.get(0).currency());
            assertEquals(100D, accountBalances.get(1).amount());
            assertEquals("USD", accountBalances.get(1).currency());
        }
    }

    private void mockCsvFile(byte[] bytes) throws IOException {
        when(multipartFile.getContentType()).thenReturn(CSV_CONTENT_TYPE);
        when(multipartFile.getBytes()).thenReturn(bytes);
    }


    private LocalDateTime parseLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
