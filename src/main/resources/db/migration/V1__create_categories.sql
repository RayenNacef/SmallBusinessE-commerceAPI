create table categories (
    id   uuid primary key,
    name varchar(100) not null
);

create unique index categories_name_unique on categories (lower(name));
