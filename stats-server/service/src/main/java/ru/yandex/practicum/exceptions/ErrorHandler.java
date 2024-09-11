package ru.yandex.practicum.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handlerAnyException(final Exception e) {
        log.error("Error ", e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Произошло непредвиденное исключени")
                .status("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerDateTimeParseException(final DateTimeParseException e) {
        log.error("Error ", e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Неверный формат даты")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerConflictValidationDataException(final ConflictException e) {
        log.error("Error ", e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Произошёл конфликт данных - неверный запрос")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        log.error("Error ", e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Отсутствует обязательный параметр запроса")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }
}