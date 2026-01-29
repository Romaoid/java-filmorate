package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Film.
 */
@Data
public class Film {
    Long id;

    @NotBlank(message = "Поле имя обязательное", groups = {Create.class})
    String name;
    String description;

    @NotNull(message = "Поле дата_релиза обязательное", groups = {Create.class})
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate releaseDate;

    @JsonFormat(pattern = "H:mm:ss")
    LocalTime duration;
}
