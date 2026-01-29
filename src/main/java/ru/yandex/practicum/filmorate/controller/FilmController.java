package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotValidException;
import ru.yandex.practicum.filmorate.model.Create;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Update;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate FIRST_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final LocalTime MINIMUM_DURATION_OF_FILM = LocalTime.of(0, 0, 1);

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Validated(Create.class) @RequestBody Film newFilm) {
        log.debug("Запрос на добавление нового фильма {}", newFilm);

        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            log.error("Ошибка добавления фильма. Поле имя отсутствует или пусто");
        }
        if (newFilm.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            throw new NotValidException("Значение поля дата_релиза должно быть позже 28.12.1895");
        }
        if (newFilm.getDescription() != null && newFilm.getDescription().length() > 200) {
            throw new NotValidException("Поле описание должно быть не более 200 символов");
        }
        if (newFilm.getDuration() != null && newFilm.getDuration().isBefore(MINIMUM_DURATION_OF_FILM)) {
            throw new NotValidException("Ошибка добавления фильма. Поле продолжительность должно быть больше ноля");
        }

        for (Film film : films.values()) {
            if (film.getName().equals(newFilm.getName()) && film.getReleaseDate().equals(newFilm.getReleaseDate())) {
                throw new DuplicatedDataException("Такой фильм уже добавлен");
            }
        }

        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Новый фильм успешно добавлен {}", newFilm);

        return newFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("Новому фильму выделен id {}", (currentMaxId + 1));
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@Validated(Update.class) @RequestBody Film newFilm) {
        log.debug("Запрос на редактирование пользователя {}", newFilm);
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            throw new NotValidException("Значение поля дата_релиза должно быть позже 28.12.1895");
        }

        for (Film film : films.values()) {
            if (film.getName().equals(newFilm.getName()) && film.getReleaseDate().equals(newFilm.getReleaseDate())) {
                throw new DuplicatedDataException("Такой фильм уже добавлен");
            }
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getName() != null) {
                log.debug("Успешное редактирование фильма {}. Значение поля имя: {}, заменено на {}",
                                        newFilm.getId(), oldFilm.getName(), newFilm.getName());
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getReleaseDate() != null) {
                if (newFilm.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
                    throw new NotValidException("Значение поля дата_релиза должно быть позже 28.12.1895");
                }
                log.debug("Успешное редактирование фильма {}. Значение поля дата_релиза: {}, заменено на {}",
                        newFilm.getId(), oldFilm.getReleaseDate(), newFilm.getReleaseDate());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getDescription() != null) {
                if (newFilm.getDescription().length() > 200) {
                    throw new NotValidException("Поле описание должно быть не более 200 символов");
                }
                log.debug("Успешное редактирование фильма {}. Значение поля описание: {}, заменено на {}",
                        newFilm.getId(), oldFilm.getDescription(), newFilm.getDescription());
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getDuration() != null) {
                if (newFilm.getDuration().isBefore(MINIMUM_DURATION_OF_FILM)) {
                    throw new NotValidException("Ошибка добавления фильма. "
                            + "Поле продолжительность должно быть больше ноля");
                }
                log.debug("Успешное редактирование фильма {}. Значение поля продолжительность: {}, заменено на {}",
                        newFilm.getId(), oldFilm.getDuration(), newFilm.getDuration());
                oldFilm.setDuration(newFilm.getDuration());
            }

            log.info("Фильм успешно отредактирован {}", oldFilm);
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }
}
