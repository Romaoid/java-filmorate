package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.dto.mapper.UserMapper;
import ru.yandex.practicum.filmorate.dto.request.UserCreateRequest;
import ru.yandex.practicum.filmorate.dto.request.UserUpdateRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;


import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage memoryUserStorage) {
        this.userStorage = memoryUserStorage;
    }

//    public void addToFriends(Long userId, Long friendId) {
//        User user = userStorage.getUserById(userId);
//        User friend = userStorage.getUserById(friendId);
//
//        if (user.getFriends() != null && user.getFriends().contains(friend.getId())) {
//            throw new ValidationException("Пользователь " + friend.getLogin() + " уже добавлен в друзья");
//}
//
//        Set<Long> friendListUser1;
//        Set<Long> friendListUser2;
//
//        if (user.getFriends() == null) {
//            friendListUser1 = new HashSet<>();
//        } else {
//            friendListUser1 = user.getFriends();
//        }
//
//        if (friend.getFriends() == null) {
//            friendListUser2 = new HashSet<>();
//        } else {
//            friendListUser2 = friend.getFriends();
//        }
//
//        friendListUser1.add(friend.getId());
//        friendListUser2.add(user.getId());
//
//        user.setFriends(friendListUser1);
//        friend.setFriends(friendListUser2);
//    }

//    public void deleteFromFriends(Long userId, Long friendId) {
//        User user = userStorage.getUserById(userId);
//        User friend = userStorage.getUserById(friendId);
//
//        if (user.getFriends() != null && user.getFriends().contains(friend.getId())) {
//            Set<Long> friendList = user.getFriends();
//            friendList.remove(friend.getId());
//            friendList = friend.getFriends();
//            friendList.remove(user.getId());
//        }
//    }

//    public Collection<User> getFriends(Long userId) {
//        User user = userStorage.getUserById(userId);
//
//        return (user.getFriends() == null) ? new ArrayList<>()
//                : userStorage.getUsers().stream()
//                .filter(friend -> user.getFriends().contains(friend.getId()))
//                .toList();
//    }

    public UserDTO create(UserCreateRequest request) {
        //проверки преобразование отправка в стораж
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ValidationException("Имейл должен быть указан");
        }
        if (request.getLogin() == null || request.getLogin().isEmpty()) {
            throw new ValidationException("Логин должен быть указан");
        }
        /* Optional<User> alreadyExistUser1 = findByEmail(request.getEmail());
        Optional<User> alreadyExistUser2 = findByLogin(request.getLogin());
        if (alreadyExistUser1.isPresent() || alreadyExistUser2.isPresent()) {
            throw new DuplicatedDataException("Данный имейлLogin уже используется");
        }*/
        User user = UserMapper.mapToUser(request);
        user = userStorage.create(user);

        return UserMapper.mapToUserDto(user);
    }

    public UserDTO update(UserUpdateRequest request) {
        //если юзера нет, будет ли ошибка из стоража?
        if (request.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }
        User updatedUser =
                UserMapper.updateUserFields(userStorage.getUserById(request.getId()), request);

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

//    public Collection<User> getFriendsMutual(Long userId, Long friendId) {
//        User user = userStorage.getUserById(userId);
//        User friend = userStorage.getUserById(friendId);
//
//        Collection<Long> mutualFriendsIdList = user.getFriends().stream()
//                .filter(id -> friend.getFriends().contains(id))
//                .toList();
//
//        return userStorage.getUsers().stream()
//                .filter(usr -> mutualFriendsIdList.contains(usr.getId()))
//                .toList();
//    }
}
