![sql diagram](src/main/resources/db.jpg?raw=true)
-
На схеме представлены две основные сущности:
- Фильм
- Пользователь

Возрастной рейтинг и жанр фильма вынесены в отдельные таблицы и имеют с таблицей _Film_ связи один 
ко многим. Для реализации связи многие ко многим таблиц _User_ и _Film_ создана таблица _Likes_. 
Статус для дружеских связей вынесен в отдельную таблицу _FriendshipStatus_ и связан с таблицей с 
таблицей _Friendship_. Посредством таблицы _Friendship_ реализуются дружеские связи пользователей.

Запрос на вывод таблицы пользователей и их состояний дружбы выглядит так:
```declarative
SELECT u1.login AS User,
    u2.login AS Friend,
    fs.name AS Status
FROM user AS u1
INNER JOIN friendship AS f 
    ON u1.user_id = f.user_id
INNER JOIN (SELECT * FROM user) AS u2 
    ON u2.user_id = f.friend_id
INNER JOIN friendship_status AS fs 
    ON f.status_id = fs.status_id
WHERE u1.user_id < u2.user_id
ORDER BY u1.login, u2.login;
```