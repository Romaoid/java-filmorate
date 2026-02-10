package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final UserStorage userStorage;

    public FilmController(FilmStorage filmStorage, FilmService filmService, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.userStorage = userStorage;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmStorage.getFilms();
    }

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable Long id) {
        return filmStorage.getFilmById(id);
    }

    @GetMapping({"/popular?count={count}", "/popular"})
    public Collection<Film> findTopFilms(@RequestParam(defaultValue = "10") String count) {
        return filmService.getTopFilms(count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film newFilm) {
        return filmStorage.create(newFilm);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmStorage.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeAdd(@PathVariable("id") Long filmId,
                       @PathVariable Long userId) {
        filmService.addLikeFilm(
                filmStorage.getFilmById(filmId),
                userStorage.getUserById(userId)
        );
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeRemove(@PathVariable("id") Long filmId,
                           @PathVariable Long userId) {
        filmService.removeLikeFilm(
                filmStorage.getFilmById(filmId),
                userStorage.getUserById(userId)
        );
    }
}
