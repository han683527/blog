drop table if exists Category;
create table Category(
    id bigint auto_increment primary key,
    category_name varchar(50) not null unique,
    create_time datetime not null default current_timestamp
);