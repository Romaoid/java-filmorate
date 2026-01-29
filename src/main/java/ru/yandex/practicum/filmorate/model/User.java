package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
public class User {
    Long id;

    @Pattern(message = "Поле имейл должно содержать буквы латинского алфавита, цифры и знак \"@\"",
            regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+.[a-zA-Z0-9_-]+",
            groups = {Create.class, Update.class})
    @NotBlank(message = "Поле имейл не должно быть пустым", groups = {Create.class})
    String email;

    @NotBlank(message = "Поле логин не должно быть пустым", groups = {Create.class})
    @Pattern(regexp = "\\S*", message = "Поле логин не должно содержать символы пробела",
            groups = {Create.class, Update.class})
    String login;
    String name;

    @Past(message = "Поле дата_рождения должно быть раньше текущей даты",
            groups = {Create.class, Update.class})
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday;
}
