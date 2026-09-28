package com.example.newsapp.core.data

import com.example.newsapp.core.data.local.ArticleEntity
import com.example.newsapp.core.data.remote.ArticleDto
import com.example.newsapp.core.data.remote.NewsListDto
import com.example.newsapp.core.domain.Article
import com.example.newsapp.core.domain.NewsList

fun ArticleDto.toArticle(): Article {
    return Article(
        articleId = article_id ?: "",
        title = title,
        description = description,
        content = content,
        pubDate = pubDate,
        sourceName = source_name,
        imageUrl = image_url
    )
}

fun NewsListDto.toNewsList(): NewsList {
    return NewsList(
        nextPage = nextPage,
        articles = results?.map { it.toArticle() } ?: emptyList()
    )
}

fun Article.toArticleEntity(): ArticleEntity {
    return ArticleEntity(
        articleId = articleId,
        title = title,
        description = description,
        content = content,
        pubDate = pubDate,
        sourceName = sourceName,
        imageUrl = imageUrl
    )
}

fun ArticleEntity.toArticle(): Article {
    return Article(
        articleId = articleId,
        title = title,
        description = description,
        content = content,
        pubDate = pubDate,
        sourceName = sourceName,
        imageUrl = imageUrl
    )
}