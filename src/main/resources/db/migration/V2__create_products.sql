create table products (
    id          uuid primary key,
    name        varchar(200)   not null,
    description varchar(2000)  not null,
    price       numeric(12, 2) not null check (price > 0),
    category_id uuid           not null references categories (id),
    active      boolean        not null
);

create index products_category_id_idx on products (category_id);
