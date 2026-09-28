package com.example.newsapp.article.presentation

sealed interface ArticleActions {
    data class LoadArticle(val articleId: String) : ArticleActions
}