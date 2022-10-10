package com.inventi.bankstatementsapi.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    public static LocalDateTime getTo(LocalDate to) {
        return to == null ? LocalDate.now().atStartOfDay() : to.atStartOfDay();
    }

    public static LocalDateTime getFrom(LocalDate from) {
        return from == null ? LocalDate.EPOCH.atStartOfDay() : from.atStartOfDay();
    }
}
