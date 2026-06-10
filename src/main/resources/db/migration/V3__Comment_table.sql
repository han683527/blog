drop table if exists Comment;
create table Comment(
    id bigint auto_increment primary key,
    content text not null,
    user_id bigint not null,    #评论者 id
    article_id bigint not null, #帖 id
    create_time datetime default CURRENT_TIMESTAMP,
    foreign key (user_id) references User(id),
    foreign key (article_id) references Article(id)
);