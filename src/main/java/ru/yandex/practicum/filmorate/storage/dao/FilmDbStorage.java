package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    protected final JdbcTemplate jdbc;
    protected final FilmRowMapper filmMapper;

    public Collection<Film> getFilms() {
        final String findAllQuery = "SELECT * FROM films";
        return jdbc.query(findAllQuery, filmMapper)
                .stream()
                .peek(film -> {
                    setRating(film);
                    getGenreFromDB(film);
                    setLikes(film);
                })
                .toList();
    }

    public Film getFilmById(Long filmId) {
        final String findByID = "SELECT * FROM films WHERE id = ?";
        try {
            Film result = jdbc.queryForObject(findByID, filmMapper, filmId);
            if (result != null) {
                setLikes(result);
                setRating(result);
                getGenreFromDB(result);
            }
            return result;
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    public Film create(Film newFilm) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        Integer rating = getRating(newFilm);
        final String insertQuery = "INSERT INTO films(title, duration, description, release_date, rating_id) " +
                "VALUES(1, 2, 3, 4, 5) returning id";

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newFilm.getName());
            ps.setInt(2, newFilm.getDuration());
            ps.setString(3, newFilm.getDescription());
            ps.setTimestamp(4, Timestamp.from(Instant.from(newFilm.getReleaseDate())));
            ps.setInt(5, rating);
            return ps;}, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            newFilm.setId(id);
            setGenreToBD(newFilm);
            return newFilm;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    public Film update(Film newFilm) {
        final String updateQuery =
                "UPDATE films " +
                "SET title = ?, duration = ?, description = ?, release_date = ?, rating_id = ? " +
                "WHERE id = ?";
        Integer rating = getRating(newFilm);

        int rowsUpdated = jdbc.update(updateQuery,
                newFilm.getName(),
                newFilm.getDuration(),
                newFilm.getDescription(),
                Timestamp.from(Instant.from(newFilm.getReleaseDate())),
                rating,
                newFilm.getId()
        );

        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }

        return getFilmById(newFilm.getId());
    }

//maybe EmptyResultDataAccessException
    private void getGenreFromDB(Film film) {
        final String findGenreQuery =
                "Select DISTINCT g.genre " +
                "FROM genre g " +
                "JOIN friendship_genres fg ON fg.genre_id = g.id " +
                "JOIN films f ON fg.film_id = f.id " +
                "WHERE f.id = ?";
        List<String> genres = jdbc.queryForList(findGenreQuery, String.class, film.getId());
        film.setGenres(genres.stream().map(Genre::from).collect(Collectors.toSet()));
    }

    private void setGenreToBD(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final String insertQuery = "INSERT INTO film_genres(genre_id, film_id) " +
                "VALUES(1, 2) returning id";
        final String getGenreId = "SELECT g.id FROM genre AS g WHERE g.genre = ?";

        Set<Integer> genresId =  film.getGenres().stream()
                .map(Genre::toString)
                .map(str -> getIdByQuery(getGenreId, str))
                .collect(Collectors.toSet());

        for (Integer genre : genresId) {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, genre);
                ps.setLong(2, film.getId());
                return ps;}, keyHolder);

            Long id = keyHolder.getKeyAs(Long.class);

            if (id == null) {
                throw new InternalServerException("Не удалось сохранить данные");
            }
        }
    }

    private void setRating(Film film) {
        final String findRatingQuery =
                "SELECT r.rating " +
                "FROM rating r " +
                "JOIN films f ON r.id = f.rating_id " +
                "WHERE f.id = ?";
        try {
            film.setRating(Rating.from(jdbc.queryForObject(findRatingQuery, String.class, film.getId())));
        } catch (EmptyResultDataAccessException ignored) {
            film.setRating(null);
        }
    }

    private Integer getRating(Film film) {
        final String findRatingQuery = "SELECT r.id FROM rating AS r WHERE r.rating = ?";
        try {
            return jdbc.queryForObject(findRatingQuery, Integer.class, film.getRating().toString());
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private Integer getIdByQuery( String query, Object key) {
        try {
            return jdbc.queryForObject(query, Integer.class, key);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private void setLikes(Film film) {
        final String findLikesQuery =
                "Select DISTINCT l.user_id " +
                "FROM likes l " +
                "JOIN films f ON l.film_id = f.id " +
                "WHERE f.id = ?";
        List<Long> likes = jdbc.queryForList(findLikesQuery, Long.class, film.getId());
        //if list.length > 0 ?
        film.setLikes(new HashSet<>(likes));
    }
}
