create table users (
    id            uuid primary key,
    email         varchar(254) not null,
    password_hash varchar(100) not null,
    role          varchar(20)  not null
);

create unique index users_email_unique on users (email);
