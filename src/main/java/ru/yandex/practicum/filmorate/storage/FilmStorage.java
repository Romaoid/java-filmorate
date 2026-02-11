package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface FilmStorage {
    final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> getFilms();

    public Film getFilmById(Long id);

    public Film create(Film newFilm);

    public Film update(Film newFilm);

    public Film delete(Film film);

}
