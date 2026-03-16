DELETE FROM likes;
DELETE FROM film_genres;
DELETE FROM friendship;
DELETE FROM films;
DELETE FROM users;
DELETE FROM rating;
DELETE FROM genre;
DELETE FROM friendship_status;

ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
ALTER TABLE likes ALTER COLUMN id RESTART WITH 1;
ALTER TABLE film_genres ALTER COLUMN id RESTART WITH 1;
ALTER TABLE friendship ALTER COLUMN id RESTART WITH 1;

MERGE INTO rating (id, rating) KEY(id) VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');
        
MERGE INTO genre (id, genre) KEY(id) VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

MERGE INTO friendship_status (id, status_name) KEY(id) VALUES
    (1, 'неподтверждённая'),
    (2, 'подтверждённая');