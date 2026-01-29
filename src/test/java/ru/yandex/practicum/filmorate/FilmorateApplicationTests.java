package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotValidException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@SpringBootTest
@WebMvcTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    //User validation tests
    @Test
    void invalid_tests_add_user() throws Exception {
        String invalidEmailUser = "{\n " +
                "\"email\": \"@test.ru\",\n" +
                "\"login\": \"testUser\"\n" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(invalidEmailUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String nullEmailUser = "{\n" + """
                email": null,
                "login": "testUser"
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(nullEmailUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String nullLoginUser = "{\n" + """
                    "email": "test@test.ru",
                    "login": null
                    }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(nullLoginUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String loginUserWithSpaseChar = "{\n" + """
                    "email": "test@test.ru",
                    "login": "test Us er"
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(loginUserWithSpaseChar)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String userFromFuture = "{\n" + """
                    "email": "test@test.ru",
                    "login": "testUser",
                    "birthday": "10.10.2030"
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(userFromFuture)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        UserController controller = new UserController();
        User user1 = new User();
        user1.setLogin("testLogin");
        user1.setEmail("test@email.com");
        controller.create(user1);

        User user2 = new User();
        user2.setLogin("testLogin");
        user2.setEmail("another@email.com");
        Exception exception = assertThrows(DuplicatedDataException.class, () -> {
            controller.create(user2);
        });
        assertEquals("Этот логин уже используется", exception.getMessage());

        User user3 = new User();
        user3.setLogin("AnotherLogin");
        user3.setEmail("test@email.com");
        exception = assertThrows(DuplicatedDataException.class, () -> {
            controller.create(user3);
        });
        assertEquals("Этот имейл уже используется", exception.getMessage());
    }

    @Test
    void invalid_tests_update_user() {
        UserController controller = new UserController();
        User user1 = new User();
        user1.setLogin("testLogin");
        user1.setEmail("test@email.com");
        controller.create(user1);

        User user2 = new User();
        user2.setLogin("testLogin");
        user2.setEmail("another@email.com");
        Exception exception = assertThrows(ConditionsNotMetException.class, () -> {
            controller.update(user2);
        });
        assertEquals("Id должен быть указан", exception.getMessage());

        user2.setId(1L);
        exception = assertThrows(DuplicatedDataException.class, () -> {
            controller.update(user2);
        });
        assertEquals("Этот логин уже используется", exception.getMessage());

        User user3 = new User();
        user3.setId(2L);
        user3.setLogin("AnotherLogin");
        exception = assertThrows(NotFoundException.class, () -> {
            controller.update(user3);
        });
        assertEquals("Пользователь с id = 2 не найден", exception.getMessage());

        user3.setId(1L);
        user3.setEmail("test@email.com");
        exception = assertThrows(DuplicatedDataException.class, () -> {
            controller.update(user3);
        });
        assertEquals("Этот имейл уже используется", exception.getMessage());
    }

    @Test
    void boundary_value_tests_birthday() throws Exception {
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String tomorrowDate = LocalDate.now().plusDays(1).format(formatterDate);
        String userFromFuture = "{\n" + """
                    "email": "test@test.ru",
                    "login": "testUser",
                    "birthday": "%s"
                }
                """.formatted(tomorrowDate);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(userFromFuture)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String todayDate = LocalDate.now().format(formatterDate);
        String userBirthToday = "{\n" + """
                    "email": "test@test.ru",
                    "login": "testUser",
                    "birthday": "%s"
                }
                """.formatted(todayDate);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(userBirthToday)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String yesterdayDate = LocalDate.now().minusDays(1).format(formatterDate);
        String userBirthYesterday = "{\n" + """
                    "email": "test@test.ru",
                    "login": "testUser",
                    "birthday": "%s"
                }
                """.formatted(yesterdayDate);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(userBirthYesterday)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        userFromFuture = "{\n" + """
                    "id": 1,
                    "birthday": "%s"
                }
                """.formatted(tomorrowDate);

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .content(userFromFuture)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        userBirthToday = "{\n" + """
                    "id": 1,
                    "birthday": "%s"
                }
                """.formatted(todayDate);

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .content(userBirthToday)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String userBirthInPast = "{\n" + """
                    "id": 1,
                    "birthday": "1990-10-10"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .content(userBirthInPast)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        userBirthYesterday = "{\n" + """
                    "id": 1,
                    "birthday": "%s"
                }
                """.formatted(yesterdayDate);

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .content(userBirthYesterday)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


    //Film validation tests
    @Test
    void invalid_tests_add_film() throws Exception {
        String blankFilmName = "{\n" + """
                    "name": "",
                    "releaseDate": "2010-10-10"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .content(blankFilmName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String nullFilmName = "{\n" + """
                    "name": null,
                    "releaseDate": "2010-10-10"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .content(nullFilmName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        String nullFilmReleaseDate = "{\n" + """
                    "name": "test",
                    "releaseDate": null
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .content(nullFilmReleaseDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void boundary_value_tests_description() throws Exception {
        String maxLengthDescriptionPlusOneMoreLetter = "Far far away, behind the word mountains, " +
                "far from the countries Vokalia and Consonantia, there live the blind texts. " +
                "Separated they live in Bookmarksgrove right at the coast of the Semantics, a large. ";

        String filmWithValidDescription = "{\n" + """
                    "name": "test1",
                    "releaseDate": "2010-10-10",
                    "description": "%s"
                }
                """.formatted(maxLengthDescriptionPlusOneMoreLetter.substring(0, 200));

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .content(filmWithValidDescription)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        String anotherFilmWithValidDescription = "{\n" + """
                    "name": "test2",
                    "releaseDate": "2011-10-10",
                    "description": "%s"
                }
                """.formatted(maxLengthDescriptionPlusOneMoreLetter.substring(0, 199));

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .content(anotherFilmWithValidDescription)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("test3");
        film.setReleaseDate(LocalDate.of(2012, 10, 10));
        film.setDescription(maxLengthDescriptionPlusOneMoreLetter);
        Exception exception = assertThrows(NotValidException.class, () -> {
            controller.create(film);
        });
        assertEquals("Поле описание должно быть не более 200 символов", exception.getMessage());

        String validDescription = "{\n" + """
                    "id": "2",
                    "description": "%s"
                }
                """.formatted(maxLengthDescriptionPlusOneMoreLetter.substring(0, 200));

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .content(validDescription)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        String validDescriptionAnother = "{\n" + """
                    "id": "1",
                    "description": "%s"
                }
                """.formatted(maxLengthDescriptionPlusOneMoreLetter.substring(0, 199));

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .content(validDescriptionAnother)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Film testFilm = new Film();
        testFilm.setName("test");
        testFilm.setReleaseDate(LocalDate.of(2012, 10, 10));
        testFilm.setDescription(maxLengthDescriptionPlusOneMoreLetter.substring(0, 199));
        controller.create(testFilm);

        film.setId(1L);
        exception = assertThrows(NotValidException.class, () -> {
            controller.update(film);
        });
        assertEquals("Поле описание должно быть не более 200 символов", exception.getMessage());
    }

    @Test
    void boundary_value_tests_releaseDate() throws Exception {
        String filmWithValidReleaseDate = "{\n" + """
                    "name": "test1",
                    "releaseDate": "1895-12-29"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .content(filmWithValidReleaseDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        filmWithValidReleaseDate = "{\n" + """
                    "name": "test2",
                    "releaseDate": "1895-12-28"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .content(filmWithValidReleaseDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        filmWithValidReleaseDate = "{\n" + """
                    "id": 1,
                    "releaseDate": "1895-12-28"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .content(filmWithValidReleaseDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        filmWithValidReleaseDate = "{\n" + """
                    "id": 2,
                    "releaseDate": "1895-12-29"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .content(filmWithValidReleaseDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("test3");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        Exception exception = assertThrows(NotValidException.class, () -> {
            controller.create(film);
        });
        assertEquals("Значение поля дата_релиза должно быть позже 28.12.1895", exception.getMessage());

        film.setReleaseDate(LocalDate.of(1896, 10, 10));
        controller.create(film);

        film.setId(1L);
        film.setName("test");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        exception = assertThrows(NotValidException.class, () -> {
            controller.update(film);
        });
        assertEquals("Значение поля дата_релиза должно быть позже 28.12.1895", exception.getMessage());
    }

    @Test
    void boundary_value_tests_duration() throws Exception {

        String filmWithValidDuration = "{\n" + """
                    "name": "test1",
                    "releaseDate": "1895-12-29",
                    "duration": 1
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .content(filmWithValidDuration)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        filmWithValidDuration = "{\n" + """
                    "id": 1,
                    "duration": 100
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .content(filmWithValidDuration)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        filmWithValidDuration = "{\n" + """
                    "id": 1,
                    "duration": 1
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .content(filmWithValidDuration)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        filmWithValidDuration = "{\n" + """
                    "id": 1,
                    "duration": -1
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .content(filmWithValidDuration)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
