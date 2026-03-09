package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.dto.mapper.UserMapper;
import ru.yandex.practicum.filmorate.dto.request.UserCreateRequest;
import ru.yandex.practicum.filmorate.dto.request.UserUpdateRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage memoryUserStorage) {
        this.userStorage = memoryUserStorage;
    }

    public void addToFriends(Long userId, Long friendId) {
        User user = getUserIfNotNull(userId);
        User friend = getUserIfNotNull(friendId);

        if (user.getFriends() != null && user.getFriends().containsKey(friend.getId())) {
            throw new ValidationException("Пользователь " + friend.getLogin() + " уже добавлен в друзья");
        }

        if (friend.getFriends() != null && friend.getFriends().containsKey(userId)) {
            userStorage.addFieldToFriendship(userId, friendId, FriendshipStatus.CONFIRMED);
            userStorage.addFieldToFriendship(friendId, userId, FriendshipStatus.CONFIRMED);
        } else {
            userStorage.addFieldToFriendship(userId, friendId, FriendshipStatus.UNCONFIRMED);
        }
    }

    public void deleteFromFriends(Long userId, Long friendId) {
        User user = getUserIfNotNull(userId);
        User friend = getUserIfNotNull(friendId);

        if (user.getFriends() != null && user.getFriends().containsKey(friend.getId())) {
            userStorage.deleteFieldFromFriendship(userId, friendId, user.getFriends().get(friendId));
        }
    }

    public Collection<UserDTO> getFriends(Long userId) {
        User user = getUserIfNotNull(userId);

        return (user.getFriends() == null || user.getFriends().isEmpty()) ? new ArrayList<>()
                : userStorage.getUsers().stream()
                .filter(friend -> user.getFriends().containsKey(friend.getId()))
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDTO create(UserCreateRequest request) {
        validateCreateRequest(request);

        User user = UserMapper.mapToUser(request);
        user = userStorage.create(user);

        return UserMapper.mapToUserDto(user);
    }

    public UserDTO update(UserUpdateRequest request) {
        if (request.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }

        User updatedUser = UserMapper.updateUserFields(getUserIfNotNull(request.getId()), request);

        return UserMapper.mapToUserDto(userStorage.update(updatedUser));
    }

    public Collection<UserDTO> getUsersAll() {
        return userStorage.getUsers()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        return UserMapper.mapToUserDto(userStorage.getUserById(id));
    }

    public Collection<UserDTO> getFriendsMutual(Long userId, Long friendId) {
        User user = getUserIfNotNull(userId);
        User friend = getUserIfNotNull(friendId);

        Collection<Long> mutualFriendsIdList = user.getFriends().keySet().stream()
                .filter(id -> friend.getFriends().containsKey(id))
                .toList();

        return userStorage.getUsers().stream()
                .filter(usr -> mutualFriendsIdList.contains(usr.getId()))
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    private void validateCreateRequest(UserCreateRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ValidationException("Email должен быть указан");
        }
        if (!(request.getEmail().matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+.[a-zA-Z0-9_-]+"))) {
            throw new ValidationException("Поле Email должно содержать буквы латинского алфавита, цифры и знак \"@\". "
                    + "Пример: example@domain.com");
        }
        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new ValidationException("Login должен быть указан");
        }
        if (!(request.getLogin().matches("\\S*"))) {
            throw new ValidationException("Поле логин не должно содержать символы пробела");
        }
        if (request.getBirthday() != null && request.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Значение поля дата_рождения должно быть раньше текущей даты");
        }

        userStorage.getUsers()
                .stream()
                .map(User::getEmail)
                .filter(email -> email.equals(request.getEmail()))
                .findFirst()
                .ifPresent(email -> {
                    throw new ValidationException("Email " + email + " уже зарегистрирован");
                });
        userStorage.getUsers()
                .stream()
                .map(User::getLogin)
                .filter(login -> login.equals(request.getLogin()))
                .findFirst()
                .ifPresent(login -> {
                    throw new ValidationException("Login " + login + " уже зарегистрирован");
                });
    }

    private User getUserIfNotNull(long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return user;
    }
}
