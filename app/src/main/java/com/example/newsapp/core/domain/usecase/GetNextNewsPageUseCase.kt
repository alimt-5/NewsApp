package com.example.newsapp.core.domain.usecase

import com.example.newsapp.core.domain.NewsRepository

class GetNextNewsPageUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(nextPage: String?) = newsRepository.pagination(nextPage)
}