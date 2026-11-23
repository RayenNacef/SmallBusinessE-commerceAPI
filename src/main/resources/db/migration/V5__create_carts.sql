create table carts (
    customer_id uuid primary key references users (id)
);

create table cart_items (
    customer_id uuid    not null references carts (customer_id) on delete cascade,
    product_id  uuid    not null references products (id),
    quantity    integer not null check (quantity > 0),
    primary key (customer_id, product_id)
);
