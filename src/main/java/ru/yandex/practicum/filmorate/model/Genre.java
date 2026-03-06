package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.ValidationException;

public enum Genre {
    COMEDY("Комедия", 1),
    DRAMA("Драма", 2),
    ANIMATION("Мультфильм", 3),
    THRILLER("Триллер", 4),
    DOCUMENTARY("Документальный", 5),
    ACTION("Боевик", 6);

    private final String serviceName;
    private final int id;

    Genre(String serviceName, int id) {
        this.serviceName = serviceName;
        this.id = id;
    }

    @Override
    public String toString() {
        return serviceName;
    }

    public int toInt() {
        return id;
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

    public static Genre from(int num) {
        return switch (num) {
            case 1 -> COMEDY;
            case 2 -> DRAMA;
            case 3 -> ANIMATION;
            case 4 -> THRILLER;
            case 5 -> DOCUMENTARY;
            case 6 -> ACTION;
            default -> throw new ValidationException("enum Genre");
        };
    }
}
