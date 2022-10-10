package com.inventi.bankstatementsapi.service;

import com.inventi.bankstatementsapi.constant.ErrorMessages;
import com.inventi.bankstatementsapi.exception.CsvImportFailedException;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvService {

    private static final String TYPE = "text/csv";

    public <T> List<T> readBeansFromCsv(MultipartFile file, Class<T> type) {
        Objects.requireNonNull(file, ErrorMessages.FILE_CANNOT_BE_NULL);
        Objects.requireNonNull(type, ErrorMessages.TYPE_CANNOT_BE_NULL);
        validateContentType(file);
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(file.getBytes()), StandardCharsets.UTF_8))) {
            validateHeader(reader.peek(), type);
            return new CsvToBeanBuilder<T>(reader)
                    .withType(type)
                    .withExceptionHandler(e -> null)
                    .withIgnoreEmptyLine(true)
                    .build()
                    .parse();
        } catch (RuntimeException | IOException e) {
            log.error("Failed to read beans from file: ", e);
            throw new CsvImportFailedException(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public <T> void writeBeansToCsv(List<T> beans, PrintWriter printWriter) {
        StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(printWriter)
                .withExceptionHandler(e -> null)
                .build();
        beanToCsv.write(beans);
    }

    private void validateContentType(MultipartFile file) {
        if (!isCsvFile(file)) {
            throw new CsvImportFailedException(ErrorMessages.IMPORT_CSV_FAILED_FILE_IS_NOT_CSV_TYPE);
        }
    }

    private boolean isCsvFile(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    private <T> void validateHeader(String[] headerFromReader, Class<T> type) {
        if (headerFromReader == null) {
            throw new CsvImportFailedException(ErrorMessages.IMPORT_CSV_FAILED_HEADER_IS_MISSING);
        }
        boolean isHeaderValid = Arrays.stream(headerFromReader)
                .allMatch(readerColumn -> isColumnValid(type, readerColumn));
        if (!isHeaderValid) {
            throw new CsvImportFailedException(ErrorMessages.IMPORT_CSV_FAILED_HEADER_IS_INVALID);
        }
    }

    private <T> boolean isColumnValid(Class<T> type, String readerColumn) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .anyMatch(expectedColumn -> expectedColumn.equalsIgnoreCase(readerColumn));
    }
}
