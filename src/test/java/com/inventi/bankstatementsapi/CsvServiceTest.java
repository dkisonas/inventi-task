package com.inventi.bankstatementsapi;

import com.inventi.bankstatementsapi.csv.BankStatementExportCsvBean;
import com.inventi.bankstatementsapi.csv.BankStatementImportCsvBean;
import com.inventi.bankstatementsapi.exception.CsvImportFailedException;
import com.inventi.bankstatementsapi.service.CsvService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsvServiceTest {

    private static final String CONTENT_TYPE_CSV = "text/csv";
    private static final String CONTENT_TYPE_PLAIN = "text/plain";

    private final MultipartFile multipartFile = mock(MultipartFile.class);

    @InjectMocks
    private CsvService csvService;

    @Nested
    @DisplayName("Csv reader")
    class CsvReader {
        @Test
        void readBeansFromCsv_whenAnyParameterIsNull_shouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> csvService.readBeansFromCsv(null, null));
            assertThrows(NullPointerException.class, () -> csvService.readBeansFromCsv(multipartFile, null));
        }

        @Test
        void readBeansFromCsv_whenFileContainCsvData_shouldReturnListOfBeans() throws IOException {
            mockCsvFile("csv/BankStatements.csv");
            assertEquals(4, csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class).size());
        }

        @Test
        void readBeansFromCsv_whenFileContainsOnlyHeader_shouldReturnEmptyList() throws IOException {
            mockCsvFile("csv/OnlyHeader.csv");
            assertEquals(0, csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class).size());
        }

        @Test
        void readBeansFromCsv_whenFileIsNotCsv_shouldThrowCsvImportFailedException() {
            when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE_PLAIN);
            assertThrows(CsvImportFailedException.class, () -> csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class));
        }

        @Test
        void readBeansFromCsv_whenFileContainsNoData_shouldThrowCsvImportFailedException() throws IOException {
            mockCsvFile("csv/EmptyFile.csv");
            assertThrows(CsvImportFailedException.class,
                    () -> csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class));
        }

        @Test
        void readBeansFromCsv_whenFileContainsOnlyCommas_shouldThrowCsvImportFailedException() throws IOException {
            mockCsvFile("csv/FileWithCommasOnly.csv");
            assertThrows(CsvImportFailedException.class,
                    () -> csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class));
        }

        @Test
        void readBeansFromCsv_whenFileContainsMismatchingHeader_shouldThrowCsvImportFailedException() throws IOException {
            mockCsvFile("csv/BankStatementsWithMismatchingHeader.csv");
            assertThrows(CsvImportFailedException.class,
                    () -> csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class));
        }

        @Test
        void readBeansFromCsv_whenFileRowsContainTooManyColumns_shouldReturnBeansWithCorrectColumnAmount() throws IOException {
            mockCsvFile("csv/BankStatementsWithTooManyColumns.csv");
            assertEquals(1, csvService.readBeansFromCsv(multipartFile, BankStatementImportCsvBean.class).size());
        }
    }

    @Nested
    @DisplayName("Csv writer")
    class CsvWriter {

        private final Path CSV_PATH = Path.of("src/test/java/test.csv");
        private File file;

        @BeforeEach
        void setup() {
            file = new File(CSV_PATH.toUri());
        }

        @AfterEach
        void destroy() {
            file.deleteOnExit();
        }

        @Test
        void writeBeansToCsv_whenBeansExist_shouldWriteBeansToFile() throws IOException {
            try (PrintWriter printWriter = new PrintWriter(file)) {
                csvService.writeBeansToCsv(List.of(
                        new BankStatementExportCsvBean(1L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 500D, "EUR"),
                        new BankStatementExportCsvBean(2L, "account3", parseLocalDateTime("2022-09-22T20:55:41"), "account4", StringUtils.EMPTY, 300D, "USD")
                ), printWriter);
            }
            try (Stream<String> linesStream = Files.lines(CSV_PATH)) {
                List<String> lines = linesStream.toList();
                assertEquals(3, lines.size());
                assertTrue(lines.get(0).contains("ACCOUNTNUMBER"));
                assertTrue(lines.get(1).contains("account1"));
                assertTrue(lines.get(2).contains("account3"));
            }
        }

        @Test
        void writeBeansToCsv_whenBeansAreEmpty_shouldWriteEmptyFile() throws IOException {
            try (PrintWriter printWriter = new PrintWriter(file)) {
                csvService.writeBeansToCsv(new ArrayList<>(), printWriter);
            }
            try (Stream<String> linesStream = Files.lines(CSV_PATH)) {
                List<String> lines = linesStream.toList();
                assertEquals(0, lines.size());
            }
        }

        @Test
        void writeBeansToCsv_whenBeanIsEmpty_shouldWriteOnlyCorrectBeansToFile() throws IOException {
            try (PrintWriter printWriter = new PrintWriter(file)) {
                List<BankStatementExportCsvBean> beans = List.of(
                        new BankStatementExportCsvBean(),
                        new BankStatementExportCsvBean(1L, "account1", parseLocalDateTime("2022-09-19T20:55:41"), "account2", "comment", 500D, "EUR"));
                csvService.writeBeansToCsv(beans, printWriter);
            }
            try (Stream<String> linesStream = Files.lines(CSV_PATH)) {
                List<String> lines = linesStream.toList();
                assertEquals(2, lines.size());
                assertTrue(lines.get(0).contains("ACCOUNTNUMBER"));
                assertTrue(lines.get(1).contains("account1"));
            }
        }
    }

    private LocalDateTime parseLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private void mockCsvFile(String fileName) throws IOException {
        when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE_CSV);
        when(multipartFile.getBytes()).thenReturn(readBytesFromFile(fileName));
    }

    private byte[] readBytesFromFile(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        try (FileInputStream fileInputStream = new FileInputStream(resource.getFile())) {
            return fileInputStream.readAllBytes();
        }
    }

}
