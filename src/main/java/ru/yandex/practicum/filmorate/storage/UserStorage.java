package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    Collection<User> getUsers();

    User getUserById(Long id);

    User create(User newUser);

    User update(User newUser);

    void addFieldToFriendship(long userId, long friendId, FriendshipStatus status);

    void deleteFieldFromFriendship(long userId, long friendId, FriendshipStatus status);
}
