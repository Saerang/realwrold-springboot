package io.realworld.article.app;

import io.realworld.article.api.dto.ArticleCreateDto;
import io.realworld.article.api.dto.MultipleArticleSearchDto;
import io.realworld.article.api.dto.SingleArticleSearchDto;
import io.realworld.article.domain.Article;

import java.util.List;

public interface ArticleService {

    Article createArticle(ArticleCreateDto articleCreateDto, long userId);

    Article getArticle(SingleArticleSearchDto dto);

    List<Article> getArticles(MultipleArticleSearchDto articleSearchDto, long userId);
}
