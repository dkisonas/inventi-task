package com.inventi.bankstatementsapi;

import com.inventi.bankstatementsapi.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


@RequiredArgsConstructor
public class DateUtilsTest {

    @Test
    void getFrom_whenLocalDateIsNull_shouldReturnEpochLocalDateTime() {
        LocalDateTime from = DateUtils.getFrom(null);
        assertEquals(LocalDate.EPOCH.atStartOfDay(), from);
    }

    @Test
    void getFrom_whenLocalDateGiven_shouldReturnLocalDateTime() {
        LocalDate fromLocalDate = LocalDate.of(2022, 1, 1);
        LocalDateTime toLocalDateTime = DateUtils.getTo(fromLocalDate);
        assertEquals(fromLocalDate.atStartOfDay(), toLocalDateTime);
    }

    @Test
    void getTo_whenLocalDateIsNull_shouldReturnLocalDateTimeNow() {
        LocalDateTime to = DateUtils.getTo(null);
        assertEquals(LocalDate.now().atStartOfDay(), to);
    }

    @Test
    void getTo_whenLocalDateGiven_shouldReturnLocalDateTime() {
        LocalDate toLocalDate = LocalDate.of(2022, 1, 1);
        LocalDateTime toLocalDateTime = DateUtils.getTo(toLocalDate);
        assertEquals(toLocalDate.atStartOfDay(), toLocalDateTime);
    }
}
