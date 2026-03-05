package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RatingDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;
    private String rating;
}
