package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreDTO;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreRowMapper implements RowMapper<GenreDTO> {
    @Override
    public GenreDTO mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        GenreDTO genre = new GenreDTO();
        genre.setId(resultSet.getLong("id"));
        genre.setName(resultSet.getString("genre"));
        return genre;
    }

}
