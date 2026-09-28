package com.example.newsapp.core.domain.usecase

import com.example.newsapp.core.domain.NewsRepository

class GetNewsUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke() = newsRepository.getNews()
}