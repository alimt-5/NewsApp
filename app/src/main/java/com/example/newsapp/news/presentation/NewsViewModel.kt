package com.example.newsapp.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.core.domain.NewsRepository
import com.example.newsapp.core.domain.NewsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private var _state = MutableStateFlow(NewsState())
    val state = _state.asStateFlow()

    init { loadNews() }

    fun onActions(actions: NewsActions) {
        when (actions) {
            NewsActions.Pagination -> pagination()
            is NewsActions.ChangeLanguage -> changeLanguage(actions.language)
        }
    }

    private fun changeLanguage(language: String) {
        if (language == _state.value.language) return

        _state.value = _state.value.copy(
            language = language,
            articleList = emptyList(),
            nextPage = null,
            isError = false
        )
        loadNews()
    }

    private fun loadNews() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            newsRepository.getNews(_state.value.language).collect { result ->
                when (result) {
                    is NewsResult.Error<*> -> {
                        _state.value = _state.value.copy(isError = true
                        )
                    }

                    is NewsResult.Success<*> -> {
                        _state.value = _state.value.copy(
                            isError = false,
                            articleList = result.data?.articles ?: emptyList(),
                            nextPage = result.data?.nextPage
                        )
                    }
                }

            }

            _state.value = _state.value.copy(
                isLoading = false
            )
        }
    }

    private fun pagination() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true
            )
            newsRepository.pagination(_state.value.nextPage, _state.value.language)
                .collect { result ->
                    when (result) {
                        is NewsResult.Error<*> -> {
                            _state.value = _state.value.copy(
                                isError = true
                            )
                        }

                        is NewsResult.Success<*> -> {
                            _state.value = _state.value.copy(
                                isError = false, articleList = _state.value.articleList.plus(
                                    result.data?.articles ?: emptyList()
                                ), nextPage = result.data?.nextPage
                            )
                        }
                    }

                }

            _state.value = _state.value.copy(
                isLoading = false
            )
        }
    }

}