create table if not exists book_order (
    order_id varchar(32) primary key,
    title varchar(255) not null,
    quantity int not null,
    unit_price_cents int not null
);

create table if not exists job_lock (
    job_name varchar(64) primary key,
    acquired_at timestamp not null
);
