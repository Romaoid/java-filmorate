package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDTO;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.dto.request.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.request.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    @Qualifier("FilmDbStorage")
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLikeFilm(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        Set<Long> ratingList;

        if (film.getLikes() == null) {
            ratingList = new HashSet<>();
        } else {
            ratingList = film.getLikes();
        }

        ratingList.add(user.getId());
        film.setLikes(ratingList);
    }

    public Collection<FilmDTO> getFilmsAll() {
        return filmStorage.getFilms()
                .stream()
                .map(FilmMapper::mapToFilmDTO)
                .collect(Collectors.toList());
    }

    public FilmDTO getFilmById(Long id) {
        return FilmMapper.mapToFilmDTO(filmStorage.getFilmById(id));
    }

    public FilmDTO create(FilmCreateRequest createRequest) {
        if (createRequest.getName() == null || createRequest.getName().isEmpty()) {
            throw new ValidationException("Название фильма должно быть указано");
        }
        //проверки на дупликат
        Film newFilm = FilmMapper.mapToFilm(createRequest);
        newFilm = filmStorage.create(newFilm);
        return FilmMapper.mapToFilmDTO(newFilm);
    }

    public FilmDTO update(FilmUpdateRequest updateRequest) {
        if (updateRequest.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }
        Film updatedFilm = FilmMapper.updateFilmFields(filmStorage.getFilmById(updateRequest.getId()),updateRequest);

        return FilmMapper.mapToFilmDTO(filmStorage.update(updatedFilm));
    }

    public void removeLikeFilm(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

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

    public Collection<Film> getFilmsTop(String count) {
        long topCount;

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