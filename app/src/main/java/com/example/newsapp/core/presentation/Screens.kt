package com.example.newsapp.core.presentation

import kotlinx.serialization.Serializable

sealed interface Screens {
    @Serializable
    data object News : Screens

    @Serializable
    data class Article(val articleId: String) : Screens
}