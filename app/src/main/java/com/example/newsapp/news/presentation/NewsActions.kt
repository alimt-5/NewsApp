package com.example.newsapp.news.presentation

sealed interface NewsActions {
    data object Pagination : NewsActions
    data class ChangeLanguage(val language: String) : NewsActions
}