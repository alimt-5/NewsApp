package com.example.newsapp.core.domain


sealed class NewsResult<T>(
    val data: T? = null,
    val error: String? = null
) {
    class Success<T>(data: T?) : NewsResult<T>(data = data, error = null)
    class Error<T>(error: String?) : NewsResult<T>(data = null, error = error)
}