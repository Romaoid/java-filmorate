package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private static final LocalDate FIRST_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Film create(Film newFilm) {
        if (isStringNull(newFilm.getName())) {
            throw new ValidationException("Поле имя не должно быть пустым");
        }

        isDateValid(newFilm.getReleaseDate());

        if (!isStringNull(newFilm.getDescription()) && newFilm.getDescription().length() > 200) {
            throw new ValidationException("Поле описание должно быть не более 200 символов");
        }
        if (newFilm.getDuration() != null && newFilm.getDuration() <= 0) {
            throw new ValidationException("Поле продолжительность должно содержать положительное число");
        }

        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Новый фильм успешно добавлен {}", newFilm);

        return newFilm;
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Поле id не должно быть пустым");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (!isStringNull(newFilm.getName())) {
                log.debug("Успешное редактирование фильма {}. Значение поля имя: {}, заменено на {}",
                        newFilm.getId(), oldFilm.getName(), newFilm.getName());
                oldFilm.setName(newFilm.getName());
            }
            if (isDateValid(newFilm.getReleaseDate())) {
                log.debug("Успешное редактирование фильма {}. Значение поля дата_релиза: {}, заменено на {}",
                        newFilm.getId(), oldFilm.getReleaseDate(), newFilm.getReleaseDate());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (!isStringNull(newFilm.getDescription())) {
                if (newFilm.getDescription().length() > 200) {
                    throw new ValidationException("Поле описание должно быть не более 200 символов");
                }
                log.debug("Успешное редактирование фильма {}. Значение поля описание: {}, заменено на {}",
                        newFilm.getId(), oldFilm.getDescription(), newFilm.getDescription());
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getDuration() != null) {
                if (newFilm.getDuration() <= 0) {
                    throw new ValidationException("Поле продолжительность должно содержать положительное число");
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
    public Film delete(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Поле id не должно быть пустым");
        }

        if (films.containsKey(film.getId())) {
            Film oldFilm = films.remove(film.getId());

            log.info("Фильм удален {}", oldFilm);
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    public Collection<Film> getFilms() {
        return films.values();
    }

    public Film getFilmById(Long id) {
        if (id == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (films.containsKey(id)) {
            return films.get(id);
        }
        throw new NotFoundException("Фильм с id = " + id + " не найден");
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

    private boolean isStringNull(String parameter) {
        return parameter == null || parameter.isBlank();
    }

    private boolean isDateValid(LocalDate date) {
        if (date == null) {
            return false;
        }

        if (date.isBefore(FIRST_FILM_RELEASE_DATE)) {
            throw new ValidationException("Значение поля дата_релиза должно быть позже 28.12.1895");
        }

        return true;
    }
}
