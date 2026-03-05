package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.ValidationException;

public enum Rating {
        G("G"),
        PG("PG"),
        PG13("PG-13"),
        R("R"),
        NC17("NC-17");

    private final String serviceName;

    Rating(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return serviceName;
    }

    public static Rating from(String rating) {
        return switch (rating.toLowerCase()) {
            case "g" -> G;
            case "pg" -> PG;
            case "pg13", "pg-13", "pg 13" -> PG13;
            case "r" -> R;
            case "nc17", "nc-17", "nc 17" -> NC17;
            default -> throw new ValidationException("enum Rating");
        };
    }
}
