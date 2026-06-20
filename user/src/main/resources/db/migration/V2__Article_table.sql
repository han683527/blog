drop table if exists Article;
create table Article(
    id bigint auto_increment primary key,
    title varchar(50) not null,
    content text not null,
    author_id bigint not null,
    create_time datetime default CURRENT_TIMESTAMP,
    foreign key (author_id) references User(id)
);