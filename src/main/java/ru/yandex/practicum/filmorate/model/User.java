package ru.yandex.practicum.filmorate.model;

import lombok.*;
import java.time.LocalDate;
import java.util.Map;

@Data
public class User {
    Long id;
    String email;
    String login;
    String name;
    LocalDate birthday;
    Map<Long, FriendshipStatus> friends;
}
