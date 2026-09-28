package com.example.newsapp.news.presentation

import com.example.newsapp.core.domain.AppLanguage

sealed interface NewsActions {
    data object Pagination : NewsActions
    data class ChangeLanguage(val language: AppLanguage) : NewsActions
}