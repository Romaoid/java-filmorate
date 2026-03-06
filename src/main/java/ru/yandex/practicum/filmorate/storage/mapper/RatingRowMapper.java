package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.RatingDTO;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RatingRowMapper implements RowMapper<RatingDTO> {
    @Override
    public RatingDTO mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        RatingDTO rating = new RatingDTO();
        rating.setId(resultSet.getInt("id"));
        rating.setRating(resultSet.getString("rating"));
        return rating;
    }
}
