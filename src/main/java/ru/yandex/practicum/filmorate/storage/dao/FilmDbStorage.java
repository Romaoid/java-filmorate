package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreDTO;
import ru.yandex.practicum.filmorate.dto.RatingDTO;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.RatingRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmMapper;
    private final RatingRowMapper ratingMapper;
    private final GenreRowMapper genreMapper;

    public Collection<Film> getFilms() {
        final String findAllQuery = "SELECT * FROM films";
        return jdbc.query(findAllQuery, filmMapper)
                .stream()
                .peek(film -> {
                    setRating(film);
                    getGenreFromDB(film);
                    setLikesFromDb(film);
                })
                .toList();
    }

    public Film getFilmById(Long filmId) {
        final String findById = "SELECT * FROM films WHERE id = ?";
        try {
            Film result = jdbc.queryForObject(findById, filmMapper, filmId);
            if (result != null) {
                setLikesFromDb(result);
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
        final String insertQuery = "INSERT INTO films(title, duration, description, release_date, rating_id) "
                + "VALUES(?, ?, ?, ?, ?)";

        jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, newFilm.getName());

                if (newFilm.getDuration() != null) {
                    ps.setInt(2, newFilm.getDuration());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }

                ps.setString(3, newFilm.getDescription());

                if (newFilm.getReleaseDate() != null) {
                    ps.setTimestamp(4, Timestamp.valueOf(newFilm.getReleaseDate().atStartOfDay()));
                } else {
                    ps.setNull(4, Types.DATE);
                }
                if (rating == null) {
                    ps.setNull(5, Types.INTEGER);
                } else {
                    ps.setInt(5, rating);
                }
                return ps;
            }, keyHolder
        );

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            newFilm.setId(id);

            if (newFilm.getGenres() != null) {
                setGenreToBD(newFilm);
            }
            return newFilm;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    public Film update(Film newFilm) {
        final String updateQuery =
                "UPDATE films "
                + "SET title = ?, duration = ?, description = ?, release_date = ?, rating_id = ? "
                + "WHERE id = ?";
        Integer rating = getRating(newFilm);

        int rowsUpdated = jdbc.update(updateQuery,
                newFilm.getName(),
                newFilm.getDuration(),
                newFilm.getDescription(),
                Timestamp.valueOf(newFilm.getReleaseDate().atStartOfDay()),
                rating,
                newFilm.getId()
        );

        setGenreToBD(newFilm);

        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }

        return getFilmById(newFilm.getId());
    }

    public void setLikeToDb(long filmId, long userId) {
        final String insertQuery = "INSERT INTO likes(user_id, film_id) VALUES(?, ?)";

        int rowsAdded = jdbc.update(insertQuery, userId, filmId);

        if (rowsAdded == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    public void deleteLikeFromDb(long filmId, long userId) {
        final String dropQuery = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";

        int rowsDropped = jdbc.update(dropQuery, userId, filmId);

        if (rowsDropped != 1) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    public Collection<RatingDTO> getRatingList() {
        final String findAllQuery = "SELECT * FROM rating";
        return jdbc.query(findAllQuery, ratingMapper);
    }

    public RatingDTO getRatingById(int id) {
        final String findById = "SELECT * FROM rating WHERE id = ?";
        try {
            return jdbc.queryForObject(findById, ratingMapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    public Collection<GenreDTO> getGenresList() {
        final String findAllQuery = "SELECT * FROM genre";
        return jdbc.query(findAllQuery, genreMapper);
    }

    public GenreDTO getGenreById(int id) {
        final String findById = "SELECT * FROM genre WHERE id = ?";
        try {
            return jdbc.queryForObject(findById, genreMapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private void getGenreFromDB(Film film) {
        final String findGenreQuery =
                "Select DISTINCT g.genre " +
                "FROM genre g " +
                "JOIN film_genres fg ON fg.genre_id = g.id " +
                "JOIN films f ON fg.film_id = f.id " +
                "WHERE f.id = ?";
        List<String> genres = jdbc.queryForList(findGenreQuery, String.class, film.getId());

        film.setGenres(genres.stream().map(Genre::from).collect(Collectors.toSet()));
    }

    private void setGenreToBD(Film film) {
        if (film.getGenres() == null) {
            return;
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final String insertQuery = "INSERT INTO film_genres(genre_id, film_id) VALUES(?, ?)";
        final String getGenreId = "SELECT g.id FROM genre AS g WHERE g.genre = ?";

        Set<Integer> genresId =  Optional.of(film.getGenres())
                .map(genres -> genres.stream()
                    .map(Genre::toString)
                    .map(str -> getIdByQuery(getGenreId, str))
                    .collect(Collectors.toSet()))
                .get();

        deleteGenreFromFilm(film);

        for (int genre : genresId) {
            jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, genre);
                    ps.setLong(2, film.getId());
                    return ps;
                }, keyHolder
            );

            Long id = keyHolder.getKeyAs(Long.class);

            if (id == null) {
                throw new InternalServerException("Не удалось сохранить данные");
            }
        }
    }

    private void deleteGenreFromFilm(Film film) {
        final String dropQuery = "DELETE FROM film_genres WHERE film_id = ?";

        jdbc.update(dropQuery, film.getId());
    }

    private void setRating(Film film) {
        final String findRatingQuery =
                "SELECT r.rating " +
                "FROM rating r " +
                "JOIN films f ON r.id = f.rating_id " +
                "WHERE f.id = ?";
        try {
            String rating = jdbc.queryForObject(findRatingQuery, String.class, film.getId());

            if (rating != null) {
                film.setRating(Rating.from(rating));
            } else {
                film.setRating(null);
            }
        } catch (EmptyResultDataAccessException ignored) {
            film.setRating(null);
        }
    }

    private Integer getRating(Film film) {
        if (film.getRating() == null) {
            return null;
        }

        final String findRatingQuery = "SELECT r.id FROM rating AS r WHERE r.rating = ?";
        try {
            return jdbc.queryForObject(findRatingQuery, Integer.class, film.getRating().toString());
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private Integer getIdByQuery(String query, String key) {
        try {
            return jdbc.queryForObject(query, Integer.class, key);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private void setLikesFromDb(Film film) {
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
