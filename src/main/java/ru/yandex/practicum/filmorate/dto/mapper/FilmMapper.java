package ru.yandex.practicum.filmorate.dto.mapper;

import org.springframework.beans.factory.annotation.Qualifier;
import ru.yandex.practicum.filmorate.dto.FilmDTO;
import ru.yandex.practicum.filmorate.dto.request.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.request.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Qualifier
public final class FilmMapper {
    public static FilmDTO mapToFilmDTO(Film film) {
        FilmDTO dto = new FilmDTO();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setDuration(film.getDuration());
        dto.setMpa(Optional.ofNullable(film.getRating()).map(Rating::toInt).orElse(null));

        dto.setGenres(film.getGenres()
                .stream()
                .map(Genre::toInt)
                .collect(Collectors.toSet())
        );

        dto.setReleaseDate(film.getReleaseDate());

        return dto;
    }

    public static Film mapToFilm(FilmCreateRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setReleaseDate(request.getReleaseDate());
        film.setRating(Optional.ofNullable(request.getMpa()).map(Rating::from).orElse(null));
        film.setDuration(request.getDuration());

        film.setGenres(Optional.ofNullable(request.getGenres())
                .map(genre -> genre.stream()
                        .map(Genre::from)
                        .collect(Collectors.toSet()))
                .orElse(new HashSet<>())
        );

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
            film.setGenres(request.getGenres()
                    .stream()
                    .map(Genre::from)
                    .collect(Collectors.toSet())
            );
        }
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasMpa()) {
            film.setRating(Rating.from(request.getMpa()));
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        return film;
    }
}
