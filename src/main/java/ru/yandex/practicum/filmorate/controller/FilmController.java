package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.getFilmsAll();
    }

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @GetMapping({"/popular?count={count}", "/popular"})
    public Collection<Film> findTopFilms(@RequestParam(defaultValue = "10") String count) {
        return filmService.getFilmsTop(count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film newFilm) {
        return filmService.create(newFilm);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeAdd(@PathVariable("id") Long filmId,
                       @PathVariable Long userId) {
        filmService.addLikeFilm(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeRemove(@PathVariable("id") Long filmId,
                           @PathVariable Long userId) {
        filmService.removeLikeFilm(filmId, userId);
    }
}
