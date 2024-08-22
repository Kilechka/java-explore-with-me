package ru.yandex.practicum.exceptions;

import lombok.Getter;

@Getter
public class ErrorResponse {
    String error;

    public ErrorResponse(String error) {
        this.error = error;
    }
}