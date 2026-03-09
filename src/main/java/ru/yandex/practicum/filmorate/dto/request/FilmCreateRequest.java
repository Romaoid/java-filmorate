package ru.yandex.practicum.filmorate.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.CrutchDTO;

import java.time.LocalDate;
import java.util.Set;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilmCreateRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<CrutchDTO> genres;
    private CrutchDTO mpa;
}
