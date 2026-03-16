package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import org.assertj.core.api.Assertions.*;
import ru.yandex.practicum.filmorate.dto.GenreDTO;
import ru.yandex.practicum.filmorate.dto.RatingDTO;
import ru.yandex.practicum.filmorate.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.RatingRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:schema.sql", "classpath:data.sql"})

class FilmorateApplicationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        UserRowMapper mapperU = new UserRowMapper();
        userStorage = new UserDbStorage(jdbcTemplate, mapperU);
        FilmRowMapper mapperF = new FilmRowMapper();
        RatingRowMapper mapperR = new RatingRowMapper();
        GenreRowMapper mapperG = new GenreRowMapper();
        filmStorage = new FilmDbStorage(jdbcTemplate, mapperF, mapperR, mapperG);
    }

//Тесты публичных методов UserDbStorage
    @Test
    public void testFindUserById() {
        assertThat(userStorage.getUserById(1L)).isNull();

        User usr = new User();
        usr.setLogin("TestUser1");
        usr.setEmail("TestUser1@test.com");
        userStorage.create(usr);

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(1L));
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testFindUsers() {

        Collection<User> users = userStorage.getUsers();

        assertThat(users).isNotNull();
        assertThat(users).isEmpty();

        User usr = new User();
        usr.setLogin("TestUser1");
        usr.setEmail("TestUser1@test.com");
        userStorage.create(usr);

        User usr2 = new User();
        usr2.setLogin("TestUser2");
        usr2.setName("Test User2");
        usr2.setEmail("TestUser2@test.com");
        usr2.setBirthday(LocalDate.of(2020,2,20));
        userStorage.create(usr2);

        users = userStorage.getUsers();

        assertThat(users).hasSize(2);

        assertThat(users)
                .extracting(User::getId, User::getLogin, User::getName, User::getEmail, User::getBirthday)
                .contains(
                        tuple(2L, "TestUser2", "Test User2", "TestUser2@test.com", LocalDate.of(2020, 2, 20))
                );
    }

    @Test
    public void testUpdateUser() {

        User usr = new User();
        usr.setLogin("TestUser1");
        usr.setEmail("TestUser1@test.com");
        userStorage.create(usr);

        User usr2 = new User();
        usr2.setId(1L);
        usr2.setLogin("updatedUser");
        usr2.setName("Test update");
        usr2.setEmail("updateUser@test.com");
        usr2.setBirthday(LocalDate.of(2020,2,20));

        userStorage.update(usr2);

        Collection<User> users = userStorage.getUsers();
        assertThat(users).hasSize(1);

        assertThat(users)
                .extracting(User::getId, User::getLogin, User::getName, User::getEmail, User::getBirthday)
                .contains(
                        tuple(1L, "updatedUser", "Test update", "updateUser@test.com", LocalDate.of(2020, 2, 20))
                );
    }

    @Test
    public void testUserAddFriend() {

        User usr = new User();
        usr.setLogin("TestUser1");
        usr.setEmail("TestUser1@test.com");
        userStorage.create(usr);

        User usr2 = new User();
        usr2.setLogin("TestUser2");
        usr2.setEmail("updateUser2@test.com");
        userStorage.create(usr2);

        User usr3 = new User();
        usr3.setLogin("TestUser3");
        usr3.setEmail("updateUser3@test.com");
        userStorage.create(usr3);

        Collection<User> users = userStorage.getUsers();

        assertThat(users)
                .hasSize(3)
                .allSatisfy(user -> {
                    assertThat(user.getFriends())
                            .isNotNull()
                            .isEmpty();
                });

        userStorage.addFieldToFriendship(1L, 2L, FriendshipStatus.UNCONFIRMED);
        userStorage.addFieldToFriendship(2L, 1L, FriendshipStatus.CONFIRMED);
        userStorage.addFieldToFriendship(1L, 2L, FriendshipStatus.CONFIRMED);
        userStorage.addFieldToFriendship(1L, 3L, FriendshipStatus.UNCONFIRMED);
        userStorage.addFieldToFriendship(3L, 2L, FriendshipStatus.UNCONFIRMED);

        users = userStorage.getUsers();
        assertThat(users).hasSize(3);

        assertThat(users)
                .filteredOn(user -> user.getId() == 1L)
                .hasSize(1)
                .first()
                .satisfies(user -> {
                    assertThat(user.getFriends())
                            .hasSize(2)
                            .containsEntry(2L, FriendshipStatus.CONFIRMED)
                            .containsEntry(3L, FriendshipStatus.UNCONFIRMED);
                });

        assertThat(users)
                .filteredOn(user -> user.getId() == 2L)
                .hasSize(1)
                .first()
                .satisfies(user -> {
                    assertThat(user.getFriends())
                            .hasSize(1)
                            .containsEntry(1L, FriendshipStatus.CONFIRMED);
                });

        assertThat(users)
                .filteredOn(user -> user.getId() == 3L)
                .hasSize(1)
                .first()
                .satisfies(user -> {
                    assertThat(user.getFriends())
                            .hasSize(1)
                            .containsEntry(2L, FriendshipStatus.UNCONFIRMED);
                });
    }

    @Test
    public void testUserDeleteFriend() {
        User usr = new User();
        usr.setLogin("TestUser1");
        usr.setEmail("TestUser1@test.com");
        userStorage.create(usr);

        User usr2 = new User();
        usr2.setLogin("TestUser2");
        usr2.setEmail("updateUser2@test.com");
        userStorage.create(usr2);

        User usr3 = new User();
        usr3.setLogin("TestUser3");
        usr3.setEmail("updateUser3@test.com");
        userStorage.create(usr3);

        userStorage.addFieldToFriendship(2L, 1L, FriendshipStatus.CONFIRMED);
        userStorage.addFieldToFriendship(1L, 2L, FriendshipStatus.CONFIRMED);
        userStorage.addFieldToFriendship(1L, 3L, FriendshipStatus.UNCONFIRMED);

        userStorage.deleteFieldFromFriendship(2L, 1L, FriendshipStatus.CONFIRMED);
        userStorage.deleteFieldFromFriendship(1L, 3L, FriendshipStatus.UNCONFIRMED);

        Collection<User> users = userStorage.getUsers();

        assertThat(users)
                .filteredOn(user -> user.getId() == 1L)
                .hasSize(1)
                .first()
                .satisfies(user -> {
                    assertThat(user.getFriends())
                            .hasSize(1)
                            .containsEntry(2L, FriendshipStatus.UNCONFIRMED);
                });
    }

//Тесты публичных методов FilmDbStorage
    @Test
    public void testFindFilmById() {
        assertThat(filmStorage.getFilmById(1L)).isNull();

        Film film = new Film();
        film.setName("Test");

        filmStorage.create(film);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(1L));
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(i ->
                        assertThat(i).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testGetFilmList() {
        Collection<Film> films = filmStorage.getFilms();

        assertThat(films).isNotNull();
        assertThat(films).isEmpty();

        Film film1 = new Film();
        film1.setName("Test film 1");
        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Test film 2");
        film2.setDescription("Film with all params");
        film2.setDuration(30);
        film2.setGenres(Set.of(Genre.ACTION,Genre.DRAMA));
        film2.setRating(Rating.G);
        film2.setReleaseDate(LocalDate.of(2020,2,20));
        filmStorage.create(film2);

        films = filmStorage.getFilms();

        assertThat(films).hasSize(2);

        assertThat(films)
                .extracting(
                        Film::getId,
                        Film::getName,
                        Film::getDescription,
                        Film::getDuration,
                        Film::getGenres,
                        Film::getRating,
                        Film::getReleaseDate
                )
                .contains(
                        tuple(2L,
                                "Test film 2",
                                "Film with all params",
                                30,
                                Set.of(Genre.ACTION, Genre.DRAMA),
                                Rating.G,
                                LocalDate.of(2020, 2, 20))
                );
    }

    @Test
    public void testUpdateFilm() {
        Film film1 = new Film();
        film1.setName("Test film 1");
        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setId(1L);
        film2.setName("Test film 2");
        film2.setDescription("Film with all params");
        film2.setDuration(30);
        film2.setGenres(Set.of(Genre.ACTION,Genre.DRAMA));
        film2.setRating(Rating.G);
        film2.setReleaseDate(LocalDate.of(2020,2,20));
        filmStorage.update(film2);

        Collection<Film> films = filmStorage.getFilms();

        assertThat(films).hasSize(1);

        assertThat(films)
                .extracting(
                        Film::getId,
                        Film::getName,
                        Film::getDescription,
                        Film::getDuration,
                        Film::getGenres,
                        Film::getRating,
                        Film::getReleaseDate
                )
                .contains(
                        tuple(1L,
                                "Test film 2",
                                "Film with all params",
                                30,
                                Set.of(Genre.ACTION, Genre.DRAMA),
                                Rating.G,
                                LocalDate.of(2020, 2, 20))
                );
    }

    @Test
    public void testAddLike() {
        User usr = new User();
        usr.setLogin("TestUser1");
        usr.setEmail("TestUser1@test.com");
        userStorage.create(usr);

        User usr2 = new User();
        usr2.setLogin("TestUser2");
        usr2.setEmail("updateUser2@test.com");
        userStorage.create(usr2);

        User usr3 = new User();
        usr3.setLogin("TestUser3");
        usr3.setEmail("updateUser3@test.com");
        userStorage.create(usr3);

        Film film1 = new Film();
        film1.setName("Test film 1");
        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Test film 2");
        filmStorage.create(film2);

        Film film3 = new Film();
        film3.setName("Test film 1");
        filmStorage.create(film3);

        filmStorage.setLikeToDb(1L, 1L);
        filmStorage.setLikeToDb(1L, 2L);
        filmStorage.setLikeToDb(1L, 3L);
        filmStorage.setLikeToDb(2L, 1L);
        filmStorage.setLikeToDb(2L, 2L);
        filmStorage.setLikeToDb(3L, 3L);

        Collection<Film> films = filmStorage.getFilms();
        assertThat(films).hasSize(3);

        assertThat(films)
                .filteredOn(film -> film.getId() == 1L)
                .hasSize(1)
                .first()
                .satisfies(film -> {
                    assertThat(film.getLikes())
                            .hasSize(3)
                            .containsExactly(1L, 2L, 3L);
                });

        assertThat(films)
                .filteredOn(film -> film.getId() == 2L)
                .hasSize(1)
                .first()
                .satisfies(film -> {
                    assertThat(film.getLikes())
                            .hasSize(2)
                            .containsExactly(1L, 2L);
                });
    }

    @Test
    public void testUnlike() {
        User usr = new User();
        usr.setLogin("TestUser1");
        usr.setEmail("TestUser1@test.com");
        userStorage.create(usr);

        User usr2 = new User();
        usr2.setLogin("TestUser2");
        usr2.setEmail("updateUser2@test.com");
        userStorage.create(usr2);

        User usr3 = new User();
        usr3.setLogin("TestUser3");
        usr3.setEmail("updateUser3@test.com");
        userStorage.create(usr3);

        Film film1 = new Film();
        film1.setName("Test film 1");
        filmStorage.create(film1);

        filmStorage.setLikeToDb(1L, 1L);
        filmStorage.setLikeToDb(1L, 2L);
        filmStorage.setLikeToDb(1L, 3L);

        Collection<Film> films = filmStorage.getFilms();

        assertThat(films)
                .filteredOn(film -> film.getId() == 1L)
                .hasSize(1)
                .first()
                .satisfies(film -> {
                    assertThat(film.getLikes())
                            .hasSize(3)
                            .containsExactly(1L, 2L, 3L);
                });

        filmStorage.deleteLikeFromDb(1L, 3L);
        films = filmStorage.getFilms();

        assertThat(films)
                .filteredOn(film -> film.getId() == 1L)
                .hasSize(1)
                .first()
                .satisfies(film -> {
                    assertThat(film.getLikes())
                            .hasSize(2)
                            .containsExactly(1L, 2L);
                });
    }

    @Test
    public void testGetRating() {
        RatingDTO dto = filmStorage.getRatingById(1);

        assertThat(dto).isNotNull();

        assertThat(dto.getId()).isEqualTo(1);

        assertThat(dto.getName())
                .satisfies(
                        name -> assertThat(name).isEqualTo("G")
                );
    }

    @Test
    public void testGetRatingList() {
        Collection<RatingDTO> ratingList = filmStorage.getRatingList();

        assertThat(ratingList)
                .hasSize(5)
                .extracting(RatingDTO::getId, RatingDTO::getName)
                .contains(
                        tuple(1, "G"),
                        tuple(2, "PG"),
                        tuple(3, "PG-13"),
                        tuple(4, "R"),
                        tuple(5, "NC-17")
                );
    }

    @Test
    public void testGetGenreById() {
        GenreDTO dto = filmStorage.getGenreById(3);

        assertThat(dto).isNotNull();

        assertThat(dto.getId()).isEqualTo(3);

        assertThat(dto.getName())
                .satisfies(
                        name -> assertThat(name).isEqualTo("Мультфильм")
                );
    }

    @Test
    public void testGetGenreList() {
        Collection<GenreDTO> genreList = filmStorage.getGenresList();

        assertThat(genreList)
                .hasSize(6)
                .extracting(GenreDTO::getId, GenreDTO::getName)
                .contains(
                        tuple(1, "Комедия"),
                        tuple(2, "Драма"),
                        tuple(3, "Мультфильм"),
                        tuple(4, "Триллер"),
                        tuple(5, "Документальный"),
                        tuple(6, "Боевик")
                );
    }
}