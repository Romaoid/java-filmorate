package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotValidException extends RuntimeException {
    public NotValidException(String message) {
        super(message);
        log.error(message);
    }
}
