package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLikeFilm(Film film, User user) {
        Set<Long> ratingList;

        if (film.getLikes() == null) {
            ratingList = new HashSet<>();
        } else {
            ratingList = film.getLikes();
        }

        ratingList.add(user.getId());
        film.setLikes(ratingList);
    }

    public void removeLikeFilm(Film film, User user) {
        if (isUserAlreadyLiked(user)) {
            if (!film.getLikes().contains(user.getId())) {
                Long likedFilmId = filmStorage.getFilms().stream()
                        .filter(filmFinder -> filmFinder.getLikes().contains(user.getId()))
                        .map(Film::getId)
                        .findFirst().orElse(null);
                throw new ValidationException("Id фильма = " + film.getId()
                        + ".\nПользователь с id = " + user.getId() + " поставил лайк фильму c id = " + likedFilmId);
            }
            film.getLikes().remove(user.getId());
            return;
        }
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не ставил лайк");
    }

    public Collection<Film> getTopFilms(String count) {
        Long topCount;
        try {
            topCount = Long.parseLong(count);

            if (topCount <= 0) {
                throw new NumberFormatException("test");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Count = " + count + ". Параметр должен быть целым положительным числом");
        }


        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film film) ->
                        Optional.ofNullable(film.getLikes())
                                .map(Set::size)
                                .orElse(0)).reversed())
                .limit(topCount)
                .collect(Collectors.toList());
    }

    private boolean isUserAlreadyLiked(User user) {
        return filmStorage.getFilms().stream()
                .anyMatch(film -> film.getLikes() != null && film.getLikes().contains(user.getId()));
    }
}