create table if not exists view_history(
  id bigint primary key auto_increment,
  user_id bigint not null,      # 属于某个用户的浏览记录
  article_id bigint not null,
  view_time DATETIME not null default CURRENT_TIMESTAMP
);