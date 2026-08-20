create table if not exists participant (
    participant_id varchar(32) primary key,
    full_name varchar(255) not null,
    email varchar(255) not null,
    course_code varchar(16) not null
);
