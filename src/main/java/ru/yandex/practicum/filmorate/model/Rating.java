package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.ValidationException;

public enum Rating {
        G("G", 1),
        PG("PG", 2),
        PG13("PG-13", 3),
        R("R", 4),
        NC17("NC-17", 5);

    private final String serviceName;
    private final int id;

    Rating(String serviceName, int id) {
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

    public static Rating from(int num) {
        return switch (num) {
            case 1-> G;
            case 2 -> PG;
            case 3 -> PG13;
            case 4 -> R;
            case 5 -> NC17;
            default -> throw new ValidationException("enum Rating");
        };
    }
}
