package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.ValidationException;

public enum Genre {
    COMEDY("Комедия"),
    DRAMA("Драма"),
    ANIMATION("Мультфильм"),
    THRILLER("Триллер"),
    DOCUMENTARY("Документальный"),
    ACTION("Боевик");

    private final String serviceName;

    Genre(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return serviceName;
    }

    public static Genre from(String genre) {
        return switch (genre.toLowerCase()) {
            case "comedy", "комедия" -> COMEDY;
            case "drama", "драма" -> DRAMA;
            case "animation", "мультфильм" -> ANIMATION;
            case "thriller", "триллер" -> THRILLER;
            case "documentary", "документальный" -> DOCUMENTARY;
            case "action", "боевик" -> ACTION;
            default -> throw new ValidationException("enum Genre");
        };
    }
}
