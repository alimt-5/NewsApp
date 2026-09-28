package com.example.newsapp.core.data.local

import android.content.Context
import com.example.newsapp.core.domain.AppLanguage
import com.example.newsapp.core.domain.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LanguageRepositoryImpl(context: Context) : LanguageRepository {
    private val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val languageState = MutableStateFlow(preferences.getString(LANGUAGE_KEY, "en").toAppLanguage())

    override fun observeLanguage(): Flow<AppLanguage> = languageState

    override suspend fun getLanguage(): AppLanguage = languageState.value

    override suspend fun setLanguage(language: AppLanguage) {
        preferences.edit().putString(LANGUAGE_KEY, language.toStoredValue()).apply()
        languageState.value = language
    }

    private fun String?.toAppLanguage(): AppLanguage = when (this) {
        "fa" -> AppLanguage.PERSIAN
        else -> AppLanguage.ENGLISH
    }

    private fun AppLanguage.toStoredValue(): String = when (this) {
        AppLanguage.ENGLISH -> "en"
        AppLanguage.PERSIAN -> "fa"
    }

    private companion object {
        const val LANGUAGE_KEY = "language"
    }
}