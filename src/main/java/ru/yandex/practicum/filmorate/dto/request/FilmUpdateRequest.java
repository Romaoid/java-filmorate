package ru.yandex.practicum.filmorate.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.CrutchDTO;

import java.time.LocalDate;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilmUpdateRequest {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<CrutchDTO> genres;
    private CrutchDTO mpa;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return !(releaseDate == null);
    }

    public boolean hasDuration() {
        return !(duration == null);
    }

    public boolean hasGenres() {
        return !(genres == null);
    }

    public boolean hasMpa() {
        return !(mpa == null);
    }
}
