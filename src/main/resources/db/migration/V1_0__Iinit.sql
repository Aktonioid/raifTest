CREATE TABLE IF NOT EXISTS customers(
    id uuid primary key,
    name varchar
);

CREATE TABLE IF NOT EXISTS accounts(
    serial_number varchar(20) primary key,
    balance numeric,
    creation_date date,
    type varchar,
    customer_id uuid REFERENCES customers(id)
);
