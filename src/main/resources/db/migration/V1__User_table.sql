drop table if exists User;
create table User(
    id bigint auto_increment primary key,
    email varchar(50) not null unique,
    password varchar(100) not null,     #加密后位数过长要加大
    nickname varchar(50) not null unique,
    create_time datetime default CURRENT_TIMESTAMP
);