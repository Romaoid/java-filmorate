package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    private UserStorage memoryUserStorage;

    public UserService(UserStorage memoryUserStorage) {
        this.memoryUserStorage = memoryUserStorage;
    }

    public void addToFriends(User user, User friend) {
        if (user.getFriends() != null && user.getFriends().contains(friend.getId())) {
            throw new ValidationException("Пользователь " + friend.getLogin() + " уже добавлен в друзья");
        }

        Set<Long> friendListUser1;
        Set<Long> friendListUser2;

        if (user.getFriends() == null) {
            friendListUser1 = new HashSet<>();
        } else {
            friendListUser1 = user.getFriends();
        }

        if (friend.getFriends() == null) {
            friendListUser2 = new HashSet<>();
        } else {
            friendListUser2 = friend.getFriends();
        }

        friendListUser1.add(friend.getId());
        friendListUser2.add(user.getId());

        user.setFriends(friendListUser1);
        friend.setFriends(friendListUser2);
    }

    public void deleteFromFriends(User user, User friend) {
        if (user.getFriends() != null && user.getFriends().contains(friend.getId())) {
            Set<Long> friendList = user.getFriends();
            friendList.remove(friend.getId());
            friendList = friend.getFriends();
            friendList.remove(user.getId());
        }
    }

    public Collection<User> getFriends(User user) {
        return (user.getFriends() == null) ? new ArrayList<>()
                : memoryUserStorage.getUsers().stream()
                .filter(friend -> user.getFriends().contains(friend.getId()))
                .toList();
    }

    public Collection<User> getMutualFriends(User user, User friend) {
        Collection<Long> mutualFriendsIdList = user.getFriends().stream()
                .filter(id -> friend.getFriends().contains(id))
                .toList();

        return memoryUserStorage.getUsers().stream()
                .filter(usr -> mutualFriendsIdList.contains(usr.getId()))
                .toList();
    }
}
