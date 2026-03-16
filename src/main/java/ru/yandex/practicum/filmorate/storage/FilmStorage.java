package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.GenreDTO;
import ru.yandex.practicum.filmorate.dto.RatingDTO;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> getFilms();

    Film getFilmById(Long id);

    Film create(Film newFilm);

    Film update(Film newFilm);

    void setLikeToDb(long filmId, long userId);

    void deleteLikeFromDb(long filmId, long userId);

    Collection<RatingDTO> getRatingList();

    RatingDTO getRatingById(int id);

    Collection<GenreDTO> getGenresList();

    GenreDTO getGenreById(int id);
}
