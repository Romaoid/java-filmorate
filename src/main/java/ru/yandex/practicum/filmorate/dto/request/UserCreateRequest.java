package ru.yandex.practicum.filmorate.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCreateRequest {
    private String login;
    private String email;
    private String name;
    private LocalDate birthday;
}
