package ru.yandex.practicum.filmorate.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmUpdateRequest {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<String> genres;
    private String mpa;

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return ! (releaseDate == null);
    }

    public boolean hasDuration() {
        return ! (duration == null);
    }

    public boolean hasGenres() {
        return ! (genres == null);
    }

    public boolean hasMpa() {
        return ! (mpa == null);
    }
}
