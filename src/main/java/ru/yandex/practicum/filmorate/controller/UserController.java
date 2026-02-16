package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> findAll() {
        return userService.getUsersAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User findUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> findFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> findCommonFriends(@PathVariable Long id,
                                        @PathVariable("otherId") Long friendId) {
        return userService.getFriendsMutual(id, friendId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(@PathVariable Long id,
                              @PathVariable Long friendId) {
        userService.addToFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriend(@PathVariable Long id,
                            @PathVariable Long friendId) {
        userService.deleteFromFriends(id, friendId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User newUser) {
        log.debug("Запрос на добавление нового пользователя {}", newUser);
        return userService.create(newUser);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User update(@RequestBody User newUser) {
        log.debug("Запрос на редактирование пользователя {}", newUser);
        return userService.update(newUser);
    }
}