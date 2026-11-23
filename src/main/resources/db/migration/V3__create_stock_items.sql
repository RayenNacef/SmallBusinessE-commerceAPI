create table stock_items (
    product_id uuid primary key references products (id),
    available  integer not null check (available >= 0)
);
