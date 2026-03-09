package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDTO;
import ru.yandex.practicum.filmorate.dto.GenreDTO;
import ru.yandex.practicum.filmorate.dto.RatingDTO;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.dto.request.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.request.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final LocalDate FIRST_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    @Qualifier("FilmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("UserDbStorage")
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLikeFilm(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        Set<Long> ratingList;

        if (film.getLikes() == null) { //нужна ли проверка на null?
            ratingList = new HashSet<>();
        } else {
            ratingList = film.getLikes();
        }

        if (ratingList.contains(user.getId())) {
            throw new ValidationException("Лайк пользователя " + user.getLogin()
                    + " уже добавлен фильму " + film.getName());
        }
        ratingList.add(user.getId());
        film.setLikes(ratingList);
        filmStorage.setLikeToDb(filmId, userId);
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
        if (createRequest.getName() == null || createRequest.getName().isBlank()) {
            throw new ValidationException("Название фильма должно быть указано");
        }
        validateDate(createRequest.getReleaseDate());
        validateDuration(createRequest.getDuration());
        validateDescription(createRequest.getDescription());

        Film newFilm = FilmMapper.mapToFilm(createRequest);
        newFilm = filmStorage.create(newFilm);
        return FilmMapper.mapToFilmDTO(newFilm);
    }

    public FilmDTO update(FilmUpdateRequest updateRequest) {
        if (updateRequest.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }

        Film updatedFilm = filmStorage.getFilmById(updateRequest.getId());

        if (updatedFilm == null) {
            throw new NotFoundException("Фильм с id = " + updateRequest.getId() + " не найден");
        }

        validateDate(updateRequest.getReleaseDate());
        validateDuration(updateRequest.getDuration());
        validateDescription(updateRequest.getDescription());

        filmStorage.getFilms()
                .stream()
                .filter(film ->
                        (film.getName().equals(updateRequest.getName()) && (film.getId() != updateRequest.getId())))
                .findFirst()
                .ifPresent(film -> {
                    throw new ValidationException("Фильм с названием " + updateRequest.getName()
                            + " уже добавлен под id " + film.getId());
                });

        updatedFilm = FilmMapper.updateFilmFields(updatedFilm, updateRequest);

        return FilmMapper.mapToFilmDTO(filmStorage.update(updatedFilm));
    }

    public void removeLikeFilm(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (isUserAlreadyLiked(user)) {
            if (!film.getLikes().contains(user.getId())) {
                Long likedFilmId = filmStorage.getFilms().stream()
                        .filter(filmFinder -> filmFinder.getLikes().contains(user.getId()))
                        .map(Film::getId)
                        .findFirst().orElse(null);
                throw new ValidationException("Id фильма = " + film.getId()
                        + ".\nПользователь с id = " + user.getId() + " поставил лайк фильму c id = " + likedFilmId);
            }

            filmStorage.deleteLikeFromDb(filmId, userId);
            film.getLikes().remove(user.getId());
            return;
        }
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не ставил лайк");
    }

    public Collection<FilmDTO> getFilmsTop(String count) {
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
                .map(FilmMapper::mapToFilmDTO)
                .collect(Collectors.toList());
    }

    public Collection<RatingDTO> getRatingList() {
        return filmStorage.getRatingList()
                .stream()
                .sorted(Comparator.comparingInt(RatingDTO::getId))
                .toList();
    }

    public RatingDTO getRatingById(int id) {
        RatingDTO dto = filmStorage.getRatingById(id);

        if (dto != null) {
            return  dto;
        }
        throw new NotFoundException("MPA с id " + id + " не найден");
    }

    public Collection<GenreDTO> getGenresList() {
        return filmStorage.getGenresList();
    }

    public GenreDTO getGenreById(int id) {
        GenreDTO dto = filmStorage.getGenreById(id);

        if (dto != null) {
            return  dto;
        }
        throw new NotFoundException("Жанр с id " + id + " не найден");

    }

    private boolean isUserAlreadyLiked(User user) {
        return filmStorage.getFilms().stream()
                .anyMatch(film -> film.getLikes() != null && film.getLikes().contains(user.getId()));
    }

    private void validateDate(LocalDate date) {
        if (date != null && date.isBefore(FIRST_FILM_RELEASE_DATE)) {
            throw new ValidationException("Значение поля дата_релиза должно быть позже 28.12.1895");
        }
    }

    private void validateDuration(Integer duration) {
        if (duration != null && duration <= 0) {
            throw new ValidationException("Поле продолжительность должно содержать положительное число");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new ValidationException("Поле описание должно быть не более 200 символов");
        }
    }
}