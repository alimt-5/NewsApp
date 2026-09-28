package com.example.newsapp.core.domain

import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getNews(language: String): Flow<NewsResult<NewsList>>
    suspend fun getArticle(articleId: String): Flow<NewsResult<Article>>
    suspend fun pagination(nextPage: String?,language: String): Flow<NewsResult<NewsList>>
}