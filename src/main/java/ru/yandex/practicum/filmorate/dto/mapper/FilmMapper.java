package ru.yandex.practicum.filmorate.dto.mapper;

import org.springframework.beans.factory.annotation.Qualifier;
import ru.yandex.practicum.filmorate.dto.FilmDTO;
import ru.yandex.practicum.filmorate.dto.request.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.request.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.model.Film;

@Qualifier
public final class FilmMapper {
    public static FilmDTO mapToFilmDTO(Film film) {
        FilmDTO dto = new FilmDTO();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setDuration(film.getDuration());
        dto.setMpa(film.getRating());
        dto.setGenres(film.getGenres());
        dto.setReleaseDate(film.getReleaseDate());

        return dto;
    }

    public static Film mapToFilm(FilmCreateRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setReleaseDate(request.getReleaseDate());
        film.setRating(request.getMpa());
        film.setDuration(request.getDuration());
        film.setGenres(request.getGenres());
        film.setDescription(request.getDescription());
        film.setLikes(null);

        return film;
    }

    public static Film updateFilmFields(Film film, FilmUpdateRequest request) {
        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }
        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        if (request.hasGenres()) {
            film.setGenres(request.getGenres());
        }
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasMpa()) {
            film.setRating(request.getMpa());
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        return film;
    }
}
