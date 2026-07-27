package org.example.blog.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.es.ArticleDocument;
import org.example.blog.es.ArticleSearchRepository;
import org.example.blog.service.ArticleSearchService;
import org.example.blog.util.UserContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleSearchServiceImpl implements ArticleSearchService {
    private final ArticleSearchRepository articleSearchRepository;
    private final ArticleQueryService articleQueryService;
    private final ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void syncArticle(Article article) {
        ArticleDocument doc = new ArticleDocument();
        doc.setId(article.getId());
        doc.setTitle(article.getTitle());
        doc.setContent(article.getContent());
        doc.setAuthorId(article.getAuthorId());
        doc.setCategoryId(article.getCategoryId());
        doc.setViewCount(article.getViewCount());
        articleSearchRepository.save(doc);
    }

    @Override
    public void deleteArticle(Long id) {
        articleSearchRepository.deleteById(id);
    }

    @Override
    public PageResponse<ArticleResponse> search(ArticleSearchRequest request) {
        String keyword = request.getKeyword();
        int page = request.getPage() - 1; // ES 页码从 0 开始的
        int size = request.getSize();

        // 构建查询(创建 ES 查询构造其,设置分页)
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
                .withPageable(PageRequest.of(page, size));

        // 有关键词 -> 全文搜索( title 的权重更高)
        // 1. 先创建 Query 对象
        // 2. 用 Query 对象设置查询
        if (keyword != null && !keyword.isEmpty()) {
            queryBuilder.withQuery(q -> q   // Query 构造器
                    .multiMatch(m -> m      // multiMatch 构造器
                            .fields("title^3", "content")
                            .query(keyword)
                    )
            );

            // 高亮配置
            Highlight highlight = new Highlight(
                    HighlightParameters.builder()
                            .withPreTags()
                            .withPostTags()
                            .build(),
                    List.of(
                            new HighlightField("title"),
                            new HighlightField("content")
                    )
            );
            queryBuilder.withHighlightQuery(new HighlightQuery(highlight, null));
        }

        // 按浏览量降序
        queryBuilder.withSort(s -> s    // Sort 构造器
                .field(f -> f           // FieldSort 构造器
                        .field("viewCount")
                        .order(SortOrder.Desc)
                )
        );

        NativeQuery query = queryBuilder.build();
        SearchHits<ArticleDocument> searchHits = elasticsearchTemplate.search(query, ArticleDocument.class);

        // 用 ES 结果构建 ArticleResponse
        List<ArticleResponse> list = searchHits.getSearchHits().stream()
                .map(hit -> {
                    ArticleDocument doc = hit.getContent();         // 取出 ES 的文档
                    ArticleResponse resp = new ArticleResponse();   // 创建接口返回对象
                    resp.setId(doc.getId());
                    resp.setTitle(doc.getTitle());
                    resp.setContent(doc.getContent());
                    resp.setAuthorId(doc.getAuthorId());
                    resp.setViewCount(doc.getViewCount());

                    // 取出高亮
                    List<String> titleHighlights = hit.getHighlightFields().get("title");
                    List<String> contentHighlights = hit.getHighlightFields().get("content");
                    if (titleHighlights != null && !titleHighlights.isEmpty()) {
                        resp.setHighlightTitle(titleHighlights.get(0));
                    }
                    if (contentHighlights != null && !contentHighlights.isEmpty()) {
                        resp.setHighlightContent(contentHighlights.get(0));
                    }

                    return resp;
                })
                .collect(Collectors.toList());

        // 填充标签/点赞/收藏/评论数/用户信息
        List<Long> articleIds = list.stream().map(ArticleResponse::getId).collect(Collectors.toList());
        articleQueryService.enrich(list, articleIds, UserContext.get());

        PageResponse<ArticleResponse> response = new PageResponse<>();
        response.setTotal(searchHits.getTotalHits());
        response.setList(list);
        return response;
    }
}
