package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.GenreDTO;
import ru.yandex.practicum.filmorate.dto.RatingDTO;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface FilmStorage {

    public Collection<Film> getFilms();

    public Film getFilmById(Long id);

    public Film create(Film newFilm);

    public Film update(Film newFilm);

    public void setLikeToDb(long filmId, long userId);

    public void deleteLikeFromDb(long filmId, long userId);

    public Collection<RatingDTO> getRatingList();

    public RatingDTO getRatingById(int id);

    public Collection<GenreDTO> getGenresList();

    public GenreDTO getGenreById(int id);
}
