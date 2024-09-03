package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlerNotFoundException(final NotFoundException e) {
        log.error("Error ", e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Данные не найдены")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerEventDateConflictException(final ConflictException e) {
        log.error("Error ", e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Произошёл конфликт данных")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handlerAnyException(final Exception e) {
        log.error("Error ", e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Произошла непредвиденная ошибка")
                .status("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
    }
}