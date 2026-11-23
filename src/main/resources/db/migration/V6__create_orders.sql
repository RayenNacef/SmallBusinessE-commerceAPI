create table orders (
    id          uuid primary key,
    customer_id uuid                     not null references users (id),
    status      varchar(20)              not null,
    placed_at   timestamp with time zone not null
);

create index orders_customer_placed_at_idx on orders (customer_id, placed_at desc);
create index orders_status_placed_at_idx on orders (status, placed_at desc);

create table order_lines (
    order_id     uuid           not null references orders (id),
    line_number  integer        not null,
    product_id   uuid           not null references products (id),
    product_name varchar(200)   not null,
    unit_price   numeric(12, 2) not null,
    quantity     integer        not null check (quantity > 0),
    primary key (order_id, line_number)
);
