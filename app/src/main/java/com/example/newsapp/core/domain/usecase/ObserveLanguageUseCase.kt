package com.example.newsapp.core.domain.usecase

import com.example.newsapp.core.domain.LanguageRepository

class ObserveLanguageUseCase(private val languageRepository: LanguageRepository) {
    operator fun invoke() = languageRepository.observeLanguage()
}