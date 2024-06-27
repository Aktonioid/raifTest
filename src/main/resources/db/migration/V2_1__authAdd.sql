ALTER TABLE customers ADD COLUMN
password varchar;
ALTER TABLE customers ADD COLUMN
username varchar UNIQUE;

CREATE TABLE IF NOT EXISTS refresh_token(
    id uuid primary key,
    expired_date date,
    refresh_token text
);

-- добавлена авторизация, так что добавил в таблицу refreshToken,а так же колонки username и password для клиентов(Customer)