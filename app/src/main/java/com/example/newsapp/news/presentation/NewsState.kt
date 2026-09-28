package com.example.newsapp.news.presentation

import com.example.newsapp.core.domain.Article

data class NewsState(
    val articleList: List<Article> = emptyList(),
    val nextPage: String? = null,
    val language: String = "en",
    val isLoading: Boolean = false,
    val isError: Boolean = false
)