package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Qualifier
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    protected final JdbcTemplate jdbc;
    protected final UserRowMapper mapper;

    public Collection<User> getUsers() {
        final String findAllQuery = "SELECT * FROM users";
        return jdbc.query(findAllQuery, mapper)
                .stream()
                .peek(this::setFriendsToUser)
                .toList();
    }

    public User getUserById(Long id) {
        final String findByID = "SELECT * FROM users WHERE id = ?";
        try {
            User necessaryUser =  jdbc.queryForObject(findByID, mapper, id);
            setFriendsToUser(necessaryUser);
            return necessaryUser;
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    public User create(User newUser) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final String insertQuery = "INSERT INTO users(login, email, name, birthday) " +
                "VALUES(?, ?, ?, ?)";

        jdbc.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, newUser.getLogin());
                    ps.setString(2, newUser.getEmail());
                    ps.setString(3, newUser.getName());

                    if (newUser.getBirthday() != null) {
                        ps.setTimestamp(4, Timestamp.valueOf(newUser.getBirthday().atStartOfDay()));
                    } else {
                        ps.setNull(4, Types.DATE);
                    }

                    return ps;
                }, keyHolder
        );

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            newUser.setId(id);
            return newUser;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    public User update(User newUser) {
        final String updateQuery =
                "UPDATE users " +
                "SET login = ?, email = ?, name = ?, birthday = ? " +
                "WHERE id = ?";
        int rowsUpdated = jdbc.update(updateQuery,
                newUser.getLogin(),
                newUser.getEmail(),
                newUser.getName(),
                Timestamp.valueOf(newUser.getBirthday().atStartOfDay()),
                newUser.getId()
        );

        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }

        return getUserById(newUser.getId());
    }

    public void addFieldToFriendship(Long userId, Long friendId, FriendshipStatus status) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final String insertQuery = "INSERT INTO friendship(user_id, friend_id, status_id) " +
                "VALUES(?, ?, ?)";
        int statusId = getFriendshipStatusId(status);

        jdbc.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, userId);
                    ps.setLong(2, friendId);
                    ps.setInt(3, statusId);
                    return ps;
                }, keyHolder
        );

        Long id = keyHolder.getKeyAs(Long.class);
        if (id == null) throw new InternalServerException("Не удалось сохранить данные");
    }

    public void deleteFieldFromFriendship(Long userId, Long friendId, FriendshipStatus status) {
        final String dropQuery = "DELETE FROM friendship WHERE UserId = ? AND FriendId = ?";
        final String mergeQuery =
                "MERGE INTO friendship(user_id, friend_id, status_id) " +
                "KEY(user_id, friend_id) " +
                "VALUES (?, ?, ?)";

        jdbc.update(dropQuery, userId, friendId);

        if (status == FriendshipStatus.CONFIRMED) {
            int statusId = getFriendshipStatusId(status);
            jdbc.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(mergeQuery);
                        ps.setLong(1, friendId);
                        ps.setLong(2, userId);
                        ps.setInt(3, statusId);
                        return ps;
                    }
            );
        }
    }

    private Integer getFriendshipStatusId(FriendshipStatus status) {
        final String findQuery = "SELECT status_id FROM friendship_status WHERE status = ?";
        return jdbc.queryForObject(findQuery, Integer.class, status.toString());
    }

    private void setFriendsToUser(User user) {
        final String findAllQuery =
                "SELECT f.friend_id AS friend, s.status AS status " +
                "FROM friendship AS f " +
                "JOIN friendship_status AS s ON f.status_id = s.id " +
                "WHERE f.user_id = ?";

        Map<Long, FriendshipStatus> friends = jdbc.query(findAllQuery, rs -> {
            Map<Long, FriendshipStatus> map = new HashMap<>();

            while (rs.next()) {
                map.put(rs.getLong("friend"), FriendshipStatus.from(rs.getString("status")));
            }
            return map;
        }, user.getId());

        user.setFriends(friends);
    }
}
