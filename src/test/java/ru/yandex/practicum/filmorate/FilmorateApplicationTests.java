package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotValidException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {
    @Test
    void contextLoads() {
    }

    //User validation tests
    @Test
    void valid_test_add_user() {
        UserController controller = new UserController();
        User user = new User();
        user.setLogin("testLogin");
        user.setEmail("test@test.com");

        User createdUser = controller.create(user);
        assertEquals(createdUser.getLogin(), createdUser.getName());
    }

    @Test
    void invalid_tests_add_user() {
        UserController controller = new UserController();

        //login == null
        User user1 = new User();

        Exception exception = assertThrows(NotValidException.class, () -> {
            controller.create(user1);
        });
        assertEquals("Поле логин не должно быть пустым", exception.getMessage());

        //login with spase
        user1.setLogin("test Login");
        exception = assertThrows(NotValidException.class, () -> {
            controller.create(user1);
        });
        assertEquals("Поле логин не должно содержать символы пробела", exception.getMessage());

        //email without username
        user1.setLogin("testLogin");
        user1.setEmail("@email.com");
        exception = assertThrows(NotValidException.class, () -> {
            controller.create(user1);
        });
        assertEquals("Поле имейл должно содержать буквы латинского алфавита, цифры и знак \"@\". " +
                "Пример: example@domain.com", exception.getMessage());

        //email without username and domain name
        user1.setEmail("@");
        exception = assertThrows(NotValidException.class, () -> {
            controller.create(user1);
        });
        assertEquals("Поле имейл должно содержать буквы латинского алфавита, цифры и знак \"@\". " +
                        "Пример: example@domain.com", exception.getMessage());

        //email == null
        User user2 = new User();
        user2.setLogin("testLogin");
        exception = assertThrows(NotValidException.class, () -> {
            controller.create(user2);
        });
        assertEquals("Поле имейл не должно быть пустым", exception.getMessage());
    }

    @Test
    void invalid_tests_update_user() {
        UserController controller = new UserController();
        User user1 = new User();
        user1.setLogin("testLogin");
        user1.setEmail("test@email.com");
        controller.create(user1);

        User user2 = new User();
        user2.setLogin("test Login");
        Exception exception = assertThrows(ConditionsNotMetException.class, () -> {
            controller.update(user2);
        });
        assertEquals("Id должен быть указан", exception.getMessage());

        user2.setId(1L);
        exception = assertThrows(NotValidException.class, () -> {
            controller.update(user2);
        });
        assertEquals("Поле логин не должно содержать символы пробела", exception.getMessage());

        User user3 = new User();
        user3.setId(2L);
        user3.setLogin("AnotherLogin");
        exception = assertThrows(NotFoundException.class, () -> {
            controller.update(user3);
        });
        assertEquals("Пользователь с id = 2 не найден", exception.getMessage());

        user3.setId(1L);
        user3.setEmail("@email.com");
        exception = assertThrows(NotValidException.class, () -> {
            controller.update(user3);
        });
        assertEquals("Поле имейл должно содержать буквы латинского алфавита, цифры и знак \"@\". " +
                "Пример: example@domain.com", exception.getMessage());
    }

    @Test
    void boundary_value_tests_birthday() {
        UserController controller = new UserController();

        User validUser1 = new User();
        validUser1.setLogin("testLogin");
        validUser1.setEmail("test@email.com");
        validUser1.setBirthday(LocalDate.now());

        User validUser2 = new User();
        validUser2.setLogin("testLogin");
        validUser2.setEmail("test@email.com");
        validUser2.setBirthday(LocalDate.now().minusDays(1));

        User invalidUser = new User();
        invalidUser.setLogin("testLogin");
        invalidUser.setEmail("test@email.com");
        invalidUser.setBirthday(LocalDate.now().plusDays(1));

        controller.create(validUser1);
        controller.create(validUser2);

        Exception exception = assertThrows(NotValidException.class, () -> {
            controller.create(invalidUser);
        });
        assertEquals("Значение поля дата_рождения должно быть раньше текущей даты", exception.getMessage());
    }

    //Film validation tests
    @Test
    void invalid_tests_film() {
        FilmController controller = new FilmController();

        //create film name == null
        Film film1 = new Film();

        Exception exception = assertThrows(NotValidException.class, () -> {
            controller.create(film1);
        });
        assertEquals("Поле имя не должно быть пустым", exception.getMessage());

        //update film id == null
        film1.setName("testFilm");
        controller.create(film1);

        Film film = new Film();

        exception = assertThrows(ConditionsNotMetException.class, () -> {
            controller.update(film);
        });
        assertEquals("Поле id не должно быть пустым", exception.getMessage());

        //update film id not exist
        film.setId(2L);

        exception = assertThrows(NotFoundException.class, () -> {
            controller.update(film);
        });
        assertEquals("Фильм с id = 2 не найден", exception.getMessage());
    }

    @Test
    void boundary_value_tests_description() {
        FilmController controller = new FilmController();

        String someText = "Far far away, behind the word mountains, " +
                "far from the countries Vokalia and Consonantia, there live the blind texts. " +
                "Separated they live in Bookmarksgrove right at the coast of the Semantics, a large. ";

        String invalidDescription = someText.substring(0, 201);
        String maxLengthDescription = someText.substring(0, 200);
        String validDescription = someText.substring(0, 199);

        //create boundary value -1 (id = 1)
        Film validFilm1 = new Film();
        validFilm1.setName("test");
        validFilm1.setDescription(validDescription);

        Film createdFilm1 = controller.create(validFilm1);
        assertEquals(validDescription, createdFilm1.getDescription());

        //create boundary value 0 (id = 2)
        Film validFilm2 = new Film();
        validFilm2.setName("test");
        validFilm2.setDescription(maxLengthDescription);

        Film createdFilm2 = controller.create(validFilm2);
        assertEquals(maxLengthDescription, createdFilm2.getDescription());

        //create boundary value +1
        Film invalidFilm = new Film();
        invalidFilm.setName("test");
        invalidFilm.setDescription(invalidDescription);

        Exception exception = assertThrows(NotValidException.class, () -> {
            controller.create(invalidFilm);
        });
        assertEquals("Поле описание должно быть не более 200 символов", exception.getMessage());

        //update boundary value 0
        Film validUpdateFilm1 = new Film();
        validUpdateFilm1.setId(1L);
        validUpdateFilm1.setDescription(maxLengthDescription);

        Film updatedFilm1 = controller.update(validUpdateFilm1);
        assertEquals(maxLengthDescription, updatedFilm1.getDescription());

        //update boundary value -1
        Film validUpdateFilm2 = new Film();
        validUpdateFilm2.setId(2L);
        validUpdateFilm2.setDescription(validDescription);

        Film updatedFilm2 = controller.update(validUpdateFilm2);
        assertEquals(validDescription, updatedFilm2.getDescription());

        //update boundary value +1
        invalidFilm.setId(1L);

        exception = assertThrows(NotValidException.class, () -> {
            controller.update(invalidFilm);
        });
        assertEquals("Поле описание должно быть не более 200 символов", exception.getMessage());
    }

    @Test
    void boundary_value_tests_releaseDate() {
        FilmController controller = new FilmController();
        LocalDate firstMovieReleaseDate = LocalDate.of(1895, 12, 28);
        //create valid boundary value 0 (id = 1)
        Film validFilm1 = new Film();
        validFilm1.setName("test3");
        validFilm1.setReleaseDate(firstMovieReleaseDate);

        Film createdFilm1 = controller.create(validFilm1);
        assertEquals(firstMovieReleaseDate, createdFilm1.getReleaseDate());

        //create valid boundary value +1 (id = 2)
        Film validFilm2 = new Film();
        validFilm2.setName("test3");
        validFilm2.setReleaseDate(firstMovieReleaseDate.plusDays(1));

        Film createdFilm2 = controller.create(validFilm2);
        assertEquals(LocalDate.of(1895, 12, 29), createdFilm2.getReleaseDate());

        //create invalid boundary value -1
        Film invalidFilm = new Film();
        invalidFilm.setName("test3");
        invalidFilm.setReleaseDate(firstMovieReleaseDate.minusDays(1));

        Exception exception = assertThrows(NotValidException.class, () -> {
            controller.create(invalidFilm);
        });
        assertEquals("Значение поля дата_релиза должно быть позже 28.12.1895", exception.getMessage());

        //update valid boundary value +1
        validFilm2.setId(1L);
        Film updatedFilm1 = controller.create(validFilm2);
        assertEquals(LocalDate.of(1895, 12, 29), updatedFilm1.getReleaseDate());

        //update valid boundary value 0
        validFilm1.setId(2L);
        Film updatedFilm2 = controller.create(validFilm1);
        assertEquals(firstMovieReleaseDate, updatedFilm2.getReleaseDate());

        //update invalid boundary value -1
        invalidFilm.setId(1L);
        exception = assertThrows(NotValidException.class, () -> {
            controller.update(invalidFilm);
        });
        assertEquals("Значение поля дата_релиза должно быть позже 28.12.1895", exception.getMessage());
    }

    @Test
    void boundary_value_tests_duration() {
        FilmController controller = new FilmController();
        //create valid boundary value 0 (id = 1)
        Film validFilm1 = new Film();
        validFilm1.setName("test3");
        validFilm1.setDuration(1);

        Film createdFilm1 = controller.create(validFilm1);
        assertEquals(1, createdFilm1.getDuration());

        //create valid boundary value +1 (id = 2)
        Film validFilm2 = new Film();
        validFilm2.setName("test3");
        validFilm2.setDuration(2);

        Film createdFilm2 = controller.create(validFilm2);
        assertEquals(2, createdFilm2.getDuration());

        //create invalid boundary value -1
        Film invalidFilm1 = new Film();
        invalidFilm1.setName("test3");
        invalidFilm1.setDuration(0);

        Exception exception = assertThrows(NotValidException.class, () -> {
            controller.create(invalidFilm1);
        });
        assertEquals("Поле продолжительность должно содержать положительное число", exception.getMessage());

        //create invalid boundary value -0
        Film invalidFilm2 = new Film();
        invalidFilm2.setName("test3");
        invalidFilm2.setDuration(-1);

        exception = assertThrows(NotValidException.class, () -> {
            controller.create(invalidFilm2);
        });
        assertEquals("Поле продолжительность должно содержать положительное число", exception.getMessage());

        //update valid boundary value +1
        validFilm2.setId(1L);
        Film updatedFilm1 = controller.create(validFilm2);
        assertEquals(2, updatedFilm1.getDuration());

        //update valid boundary value 0
        validFilm1.setId(2L);
        Film updatedFilm2 = controller.create(validFilm1);
        assertEquals(1, updatedFilm2.getDuration());

        //update invalid boundary value -1
        invalidFilm1.setId(1L);
        exception = assertThrows(NotValidException.class, () -> {
            controller.update(invalidFilm1);
        });
        assertEquals("Поле продолжительность должно содержать положительное число", exception.getMessage());

        //update invalid boundary value -2
        invalidFilm2.setId(1L);
        exception = assertThrows(NotValidException.class, () -> {
            controller.update(invalidFilm2);
        });
        assertEquals("Поле продолжительность должно содержать положительное число", exception.getMessage());
    }
}
