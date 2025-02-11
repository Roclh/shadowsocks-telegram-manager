package org.Roclh.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateTimeUtils {

    @Nullable
    public static LocalDateTime parse(@NonNull String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse DateTime: {} ", dateTime, e);
            return null;
        }
    }
}