create table if not exists book_order (
    order_id varchar(32) primary key,
    title varchar(255) not null,
    quantity int not null,
    unit_price_cents int not null
);
