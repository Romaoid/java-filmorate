package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotValidException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User newUser) {
        log.debug("Запрос на добавление нового пользователя {}", newUser);

        if (!isLoginValid(newUser.getLogin())) {
            throw new NotValidException("Поле логин не должно быть пустым");
        }

        isDateValid(newUser.getBirthday());

        if (!isEmailValid(newUser.getEmail())) {
            throw new NotValidException("Поле имейл не должно быть пустым");
        }

        if (isStringNull(newUser.getName())) {
            log.trace("Полю имя присвоено значение {}", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }

        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Новый пользователь успешно зарегистрирован {}", newUser);

        return newUser;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.debug("Запрос на редактирование пользователя {}", newUser);
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (isEmailValid(newUser.getEmail())) {
                log.debug("Успешное редактирование пользователя {}. Значение поля имейл: {}, заменено на {}",
                        newUser.getId(), oldUser.getEmail(), newUser.getEmail());
                oldUser.setEmail(newUser.getEmail());
            }
            if (isLoginValid(newUser.getLogin())) {
                log.debug("Успешное редактирование пользователя {}. Значение поля логин: {}, заменено на {}",
                        newUser.getId(), oldUser.getLogin(), newUser.getLogin());
                oldUser.setLogin(newUser.getLogin());
            }
            if (!isStringNull(newUser.getName())) {
                log.debug("Успешное редактирование пользователя {}. Значение поля имя: {}, заменено на {}",
                        newUser.getId(), oldUser.getName(), newUser.getName());
                oldUser.setName(newUser.getName());
            }
            if (isDateValid(newUser.getBirthday())) {
                log.debug("Успешное редактирование пользователя {}. Значение поля дата_рождения: {}, заменено на {}",
                        newUser.getId(), oldUser.getBirthday(), newUser.getBirthday());
                oldUser.setBirthday(newUser.getBirthday());
            }

            log.info("Пользователь успешно отредактирован {}", oldUser);

            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private boolean isStringNull(String parameter) {
        return parameter == null || parameter.isBlank();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("Новому пользователю выделен id {}", (currentMaxId + 1));
        return ++currentMaxId;
    }

    private boolean isLoginValid(String login) {
        if (isStringNull(login)) {
            return false;
        }

        if (login.matches("\\S*")) {
            return true;
        }

        throw new NotValidException("Поле логин не должно содержать символы пробела");
    }

    private boolean isEmailValid(String email) {
        if (isStringNull(email)) {
            return false;
        }

        if (email.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+.[a-zA-Z0-9_-]+")) {
            return true;
        }

        throw new NotValidException("Поле имейл должно содержать буквы латинского алфавита, цифры и знак \"@\". " +
                "Пример: example@domain.com");
    }

    private boolean isDateValid(LocalDate date) {
        if (date == null) {
            return false;
        }

        if (date.isAfter(LocalDate.now())) {
            throw new NotValidException("Значение поля дата_рождения должно быть раньше текущей даты");
        }

        return true;
    }
}