package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;

@Component
@Qualifier
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    protected final JdbcTemplate jdbc;
    protected final UserRowMapper mapper;

    public Collection<User> getUsers() {
        final String findAllQuery = "SELECT * FROM users";
        return jdbc.query(findAllQuery, mapper);
        /*
         .stream()
                .peek(user -> setFriends(user))
                .toList();
         */
    }

    public User getUserById(Long id) {
        final String findByID = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbc.queryForObject(findByID, mapper, id);
            //setFriendsToUser
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    public User create(User newUser) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final String insertQuery = "INSERT INTO users(login, email, name, birthday) " +
                "VALUES(1, 2, 3, 4) returning id";

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newUser.getLogin());
            ps.setString(2, newUser.getEmail());
            ps.setString(3, newUser.getName());
            ps.setTimestamp(4, Timestamp.from(Instant.from(newUser.getBirthday())));
            return ps;}, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            newUser.setId(id);
            return newUser;
        } else {
            throw new serverErrorResponse("Не удалось сохранить данные");
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
                Timestamp.from(Instant.from(newUser.getBirthday())),
                newUser.getId()
        );

        if (rowsUpdated == 0) {
            throw new serverErrorResponse("Не удалось обновить данные");
        }

        return getUserById(newUser.getId());
    }

    //private void setFriendsToUser(User user){}
}
