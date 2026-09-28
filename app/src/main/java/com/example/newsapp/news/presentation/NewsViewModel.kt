package com.example.newsapp.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.core.domain.AppLanguage
import com.example.newsapp.core.domain.NetworkMonitor
import com.example.newsapp.core.domain.NewsResult
import com.example.newsapp.core.domain.usecase.GetNextNewsPageUseCase
import com.example.newsapp.core.domain.usecase.GetNewsUseCase
import com.example.newsapp.core.domain.usecase.ObserveLanguageUseCase
import com.example.newsapp.core.domain.usecase.SetLanguageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsViewModel(
    private val getNewsUseCase: GetNewsUseCase,
    private val getNextNewsPageUseCase: GetNextNewsPageUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(NewsState())
    val state = _state.asStateFlow()

    init {
        observeLanguageAndNetwork()
    }

    fun onActions(actions: NewsActions) {
        when (actions) {
            NewsActions.Pagination -> pagination()
            is NewsActions.ChangeLanguage -> changeLanguage(actions.language)
        }
    }

    private fun observeLanguageAndNetwork() {
        viewModelScope.launch {
            combine(
                observeLanguageUseCase(),
                networkMonitor.isOnline
            ) { language, isOnline ->
                language to isOnline
            }.distinctUntilChanged().collectLatest { (language, isOnline) ->
                _state.update {
                    it.copy(
                        language = language,
                        isOnline = isOnline,
                        articleList = if (it.language != language) emptyList() else it.articleList,
                        nextPage = if (it.language != language) null else it.nextPage,
                        isError = false
                    )
                }

                loadNews()
            }
        }
    }

    private fun changeLanguage(language: AppLanguage) {
        if (
            !_state.value.isOnline ||
            !networkMonitor.isCurrentlyOnline() ||
            language == _state.value.language
        ) return

        viewModelScope.launch {
            setLanguageUseCase(language)
        }
    }

    private suspend fun loadNews() {
        _state.update {
            it.copy(isLoading = true)
        }

        getNewsUseCase().collect { result ->
            when (result) {
                is NewsResult.Error<*> -> {
                    _state.update {
                        it.copy(isError = true)
                    }
                }

                is NewsResult.Success<*> -> {
                    _state.update {
                        it.copy(
                            isError = false,
                            articleList = result.data?.articles ?: emptyList(),
                            nextPage = result.data?.nextPage
                        )
                    }
                }
            }
        }

        _state.update {
            it.copy(isLoading = false)
        }
    }

    private fun pagination() {
        val currentState = _state.value
        val nextPage = currentState.nextPage

        if (
            !currentState.isOnline ||
            currentState.isLoading ||
            nextPage.isNullOrBlank()
        ) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }

            getNextNewsPageUseCase(nextPage).collect { result ->
                when (result) {
                    is NewsResult.Error<*> -> {
                        _state.update {
                            it.copy(isError = true)
                        }
                    }

                    is NewsResult.Success<*> -> {
                        _state.update {
                            it.copy(
                                isError = false,
                                articleList = it.articleList + (result.data?.articles ?: emptyList()),
                                nextPage = result.data?.nextPage
                            )
                        }
                    }
                }
            }

            _state.update {
                it.copy(isLoading = false)
            }
        }
    }
}