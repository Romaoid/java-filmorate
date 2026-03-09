package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.NotFoundException;


public enum FriendshipStatus {
    UNCONFIRMED("неподтверждённая"),
    CONFIRMED("подтверждённая");

    private final String serviceName;

    FriendshipStatus(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return serviceName;
    }

    public static FriendshipStatus from(String status) {
        return switch (status.toLowerCase()) {
            case "неподтверждённая" -> UNCONFIRMED;
            case "подтверждённая" -> CONFIRMED;
            default -> throw new NotFoundException("Не найден Status: " + status);
        };
    }
}
