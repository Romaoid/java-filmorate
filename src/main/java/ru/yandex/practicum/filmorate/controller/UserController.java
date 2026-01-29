package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Create;
import ru.yandex.practicum.filmorate.model.Update;
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
    public User create(@Validated(Create.class) @RequestBody User newUser) {
        log.debug("Запрос на добавление нового пользователя {}", newUser);

        if (newUser.getLogin() == null || newUser.getLogin().isBlank()) {
        log.error("Ошибка добавления пользователя. Поле логин отсутствует или пусто");
        }
        if (newUser.getLogin().contains(" ")) {
            log.error("Ошибка добавления пользователя. Поле логин содержит знаки - \"пробел\"");
        }
        if (newUser.getBirthday() != null && newUser.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка добавления пользователя. Поле дата_рождения старше текущей даты");
        }
        if (!newUser.getEmail().contains("@")) {
            log.error("Ошибка добавления пользователя. Поле имейл не содержит знак - \"@\"");
        }

        for (User user : users.values()) {
            if (user.getEmail().equals(newUser.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            if (user.getLogin().equals(newUser.getLogin())) {
                throw new DuplicatedDataException("Этот логин уже используется");
            }
        }

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.trace("Полю имя присвоено значение {}", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }

        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Новый пользователь успешно зарегистрирован {}", newUser);

        return newUser;
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

    @PutMapping
    public User update(@Validated(Update.class) @RequestBody User newUser) {
        log.debug("Запрос на редактирование пользователя {}", newUser);
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        for (User user : users.values()) {
            if (user.getEmail().equals(newUser.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            if (user.getLogin().equals(newUser.getLogin())) {
                throw new DuplicatedDataException("Этот логин уже используется");
            }
        }

        if (newUser.getBirthday() != null && newUser.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка изменения пользователя. Поле дата_рождения старше текущей даты");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (newUser.getEmail() != null) {
                log.debug("Успешное редактирование пользователя {}. Значение поля имейл: {}, заменено на {}",
                        newUser.getId(), oldUser.getEmail(), newUser.getEmail());
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null) {
                log.debug("Успешное редактирование пользователя {}. Значение поля логин: {}, заменено на {}",
                        newUser.getId(), oldUser.getLogin(), newUser.getLogin());
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null) {
                log.debug("Успешное редактирование пользователя {}. Значение поля имя: {}, заменено на {}",
                        newUser.getId(), oldUser.getName(), newUser.getName());
                oldUser.setName(newUser.getName());
            }
            if (newUser.getBirthday() != null) {
                log.debug("Успешное редактирование пользователя {}. Значение поля дата_рождения: {}, заменено на {}",
                        newUser.getId(), oldUser.getBirthday(), newUser.getBirthday());
                oldUser.setBirthday(newUser.getBirthday());
            }

            log.info("Пользователь успешно отредактирован {}", oldUser);

            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }


}
