drop table if exists Article_Tag;
create table Article_Tag(
    article_id bigint not null,
    tag_id bigint not null,
    primary key (article_id,tag_id)
);