package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface UserStorage {
    final Map<Long, User> users = new HashMap<>();

    public Collection<User> getUsers();

    public User getUserById(Long id);

    public User create(User newUser);

    public User update(User newUser);

    public void addFieldToFriendship(long userId, long friendId, FriendshipStatus status);

    public void deleteFieldFromFriendship(long userId, long friendId, FriendshipStatus status);
}
