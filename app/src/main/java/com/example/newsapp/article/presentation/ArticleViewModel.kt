package com.example.newsapp.article.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.core.domain.NewsResult
import com.example.newsapp.core.domain.usecase.GetArticleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArticleViewModel(
    private val getArticleUseCase: GetArticleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleState())
    val state = _state.asStateFlow()

    fun onAction(action: ArticleActions) {
        when (action) {
            is ArticleActions.LoadArticle -> loadArticle(action.articleId)
        }
    }

    private fun loadArticle(articleId: String) {
        if (articleId.isEmpty()) {
            _state.value = _state.value.copy(isError = true)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                isError = false
            )

            getArticleUseCase(articleId).collect { result ->
                when (result) {
                    is NewsResult.Error<*> -> {
                        _state.value = _state.value.copy(isError = true)
                    }

                    is NewsResult.Success<*> -> {
                        _state.value = _state.value.copy(
                            isError = false,
                            article = result.data
                        )
                    }
                }
            }

            _state.value = _state.value.copy(isLoading = false)
        }
    }
}