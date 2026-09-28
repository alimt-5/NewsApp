package com.example.newsapp.core.domain

import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun getLanguage(): AppLanguage
    suspend fun setLanguage(language: AppLanguage)
}