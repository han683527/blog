drop table if exists Tag;
create table Tag(
    id bigint auto_increment primary key,
    tag_name varchar(50) not null unique,
    create_time datetime not null default current_timestamp
);
