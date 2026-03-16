package ru.yandex.practicum.filmorate.dto.mapper;

import org.springframework.beans.factory.annotation.Qualifier;
import ru.yandex.practicum.filmorate.dto.CrutchDTO;
import ru.yandex.practicum.filmorate.dto.FilmDTO;
import ru.yandex.practicum.filmorate.dto.GenreDTO;
import ru.yandex.practicum.filmorate.dto.RatingDTO;
import ru.yandex.practicum.filmorate.dto.request.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.request.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
        dto.setMpa(Optional.ofNullable(mapToRatingDTO(film.getRating())).orElse(null));

        dto.setGenres(film.getGenres()
                .stream()
                .map(FilmMapper::mapToGenreDTO)
                .sorted(Comparator.comparingLong(GenreDTO::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new))
        );

        dto.setReleaseDate(film.getReleaseDate());

        return dto;
    }

    public static Film mapToFilm(FilmCreateRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setReleaseDate(request.getReleaseDate());
        film.setRating(mapToRating(request.getMpa()));
        film.setDuration(request.getDuration());

        film.setGenres(Optional.ofNullable(request.getGenres())
                .map(genre -> genre.stream()
                        .map(FilmMapper::mapToGenre)
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
                    .map(genre -> mapToGenre(genre))
                    .collect(Collectors.toSet())
            );
        }
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasMpa()) {
            film.setRating(Optional.ofNullable(mapToRating(request.getMpa())).orElse(null));
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        return film;
    }

    private static Rating mapToRating(CrutchDTO dto) {
        if (dto == null || dto.getId() == null) {
            return null;
        }
        return Rating.from(dto.getId());
    }

    private static RatingDTO mapToRatingDTO(Rating rating) {
        if (rating == null) {
            return null;
        }
        RatingDTO mpa = new RatingDTO();
        mpa.setId(rating.toInt());
        mpa.setName(rating.toString());
        return mpa;
    }

    private static Genre mapToGenre(CrutchDTO dto) {
        if (dto == null || dto.getId() == null) {
            return null;
        }
        return Genre.from(dto.getId());
    }

    private static GenreDTO mapToGenreDTO(Genre genre) {
        if (genre == null) {
            return null;
        }
        GenreDTO dto = new GenreDTO();
        dto.setId(genre.toInt());
        dto.setName(genre.toString());
        return dto;
    }
}
