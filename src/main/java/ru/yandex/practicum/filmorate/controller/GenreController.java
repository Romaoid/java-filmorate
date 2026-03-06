package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDTO;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
    private final FilmService filmService;

    @GetMapping
    public Collection<GenreDTO> findAll() {
        return filmService.getGenresList();
    }

    @GetMapping("/{id}")
    public GenreDTO findGenreById(@PathVariable int id) {
        return filmService.getGenreById(id);
    }
}
