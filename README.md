![sql diagram](src/main/resources/db.jpg?raw=true)
-
На схеме представлены две основные сущности:
- Фильм
- Пользователь

Возрастной рейтинг фильма вынесен в отдельную таблицу со связью один-ко-многим. 
Для реализации связи многие-ко-многим таблиц _user_ и _film_ создана таблица _likes_ и таблица 
_film_rating_ для таблиц _film_ и _rating_mpa_. 
Статус для дружеских связей вынесен в отдельную таблицу _friendship_status_ и связан с таблицей с 
таблицей _friendship_. Посредством таблицы _friendship_ реализуются дружеские связи пользователей.

Запрос на вывод таблицы пользователей и их состояний дружбы выглядит так:
```sql
SELECT u1.login AS User,
    u2.login AS Friend,
    fs.status_name AS Status
FROM user AS u1
INNER JOIN friendship AS f 
    ON u1.id = f.user_id
INNER JOIN (SELECT * FROM user) AS u2 
    ON u2.id = f.friend_id
INNER JOIN friendship_status AS fs 
    ON f.status_id = fs.id
WHERE u1.user_id < u2.user_id
ORDER BY u1.login, u2.login;
```