package com.example.newsapp.news.presentation

import com.example.newsapp.core.domain.AppLanguage
import com.example.newsapp.core.domain.Article

data class NewsState(
    val articleList: List<Article> = emptyList(),
    val nextPage: String? = null,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val isOnline: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)