package ru.yandex.practicum.exception;

public class ConflictDataException extends RuntimeException {
    public ConflictDataException(final String message) {
        super(message);
    }
}