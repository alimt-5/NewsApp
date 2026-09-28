package com.example.newsapp.core.domain.usecase

import com.example.newsapp.core.domain.NewsRepository

class GetArticleUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(articleId: String) = newsRepository.getArticle(articleId)
}