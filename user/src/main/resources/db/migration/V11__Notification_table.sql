drop table if exists Notification;
create table Notification(
    id bigint auto_increment primary key,
    user_id bigint not null, # 接收通知的人
    actor_id bigint not null, # 执行操作的人(点赞/收藏/评论)
    type varchar(20) not null, #操作类型(点赞/收藏/评论)
    article_id bigint not null,
    is_read tinyint(1) not null default 0, # 是否已读
    create_time datetime not null default current_timestamp
);