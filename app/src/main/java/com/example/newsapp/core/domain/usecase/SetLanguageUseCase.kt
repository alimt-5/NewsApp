package com.example.newsapp.core.domain.usecase

import com.example.newsapp.core.domain.AppLanguage
import com.example.newsapp.core.domain.LanguageRepository

class SetLanguageUseCase(private val languageRepository: LanguageRepository) {
    suspend operator fun invoke(language: AppLanguage) = languageRepository.setLanguage(language)
}