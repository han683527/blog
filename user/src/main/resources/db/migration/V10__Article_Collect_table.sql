drop table if exists Article_Collect;
create table Article_Collect(
    user_id bigint not null,
    article_id bigint not null,
    create_time datetime not null default current_timestamp,
    primary key (user_id,article_id)
);