package ru.yandex.practicum.filmorate.dto.request;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.Set;


@Data
public class FilmCreateRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Genre> genres;
    private Rating mpa;
}
