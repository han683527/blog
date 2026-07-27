package org.example.blog.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "articles")
public class ArticleDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Long)
    private Long authorId;

    @Field(type = FieldType.Keyword)
    private String authorNickname;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Long)
    private Long commentCount;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Long)
    private Long collectCount;

    @Field(type = FieldType.Keyword)
    private String tags;
}
