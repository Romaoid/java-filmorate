package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.RatingDTO;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class RatingController {
    private final FilmService filmService;

    @GetMapping
    public Collection<RatingDTO> findAll() {
        return filmService.getRatingList();
    }

    @GetMapping("/{id}")
    public RatingDTO findRatingById(@PathVariable int id) {
        return filmService.getRatingById(id);
    }
}
