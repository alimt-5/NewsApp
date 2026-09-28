package com.example.newsapp.core.data.repository

import com.example.newsapp.BuildConfig
import com.example.newsapp.core.data.Config.BASE_URL
import com.example.newsapp.core.data.local.ArticleDao
import com.example.newsapp.core.data.remote.NewsListDto
import com.example.newsapp.core.data.toArticle
import com.example.newsapp.core.data.toArticleEntity
import com.example.newsapp.core.data.toNewsList
import com.example.newsapp.core.domain.AppLanguage
import com.example.newsapp.core.domain.Article
import com.example.newsapp.core.domain.LanguageRepository
import com.example.newsapp.core.domain.NetworkMonitor
import com.example.newsapp.core.domain.NewsList
import com.example.newsapp.core.domain.NewsRepository
import com.example.newsapp.core.domain.NewsResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NewsRepositoryImpl(
    private val httpClient: HttpClient,
    private val articleDao: ArticleDao,
    private val languageRepository: LanguageRepository,
    private val networkMonitor: NetworkMonitor
) : NewsRepository {

    private suspend fun getRemote(nextPage: String?, language: String): NewsList {
        val remoteNews: NewsListDto = httpClient.get(BASE_URL) {
            parameter("apikey", BuildConfig.API_KEY)
            parameter("language", language)
            if (nextPage != null) parameter("page", nextPage)
        }.body()
        return remoteNews.toNewsList()
    }

    private suspend fun getLocal(nextPage: String?): NewsList {
        return NewsList(
            nextPage = nextPage,
            articles = articleDao.getArticleList().map { it.toArticle() }
        )
    }

    override suspend fun getNews(): Flow<NewsResult<NewsList>> = flow {
        val language = languageRepository.getLanguage().toApiCode()
        if (!networkMonitor.isCurrentlyOnline()) {
            val localNewsList = getLocal(null)
            if (localNewsList.articles.isNotEmpty()) {
                emit(NewsResult.Success(localNewsList))
            } else {
                emit(NewsResult.Error("No Data"))
            }
            return@flow
        }
        val remoteNewsList = try {
            getRemote(null, language)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }

        remoteNewsList?.let {
            articleDao.clearDatabase()
            articleDao.upsertArticle(
                it.articles.map { article -> article.toArticleEntity() }
            )
            emit(NewsResult.Success(it))
            return@flow
        }

        val localNewsList = getLocal(null)
        if (localNewsList.articles.isNotEmpty()) {
            emit(NewsResult.Success(localNewsList))
        } else {
            emit(NewsResult.Error("No Data"))
        }
    }

    override suspend fun getArticle(articleId: String): Flow<NewsResult<Article>> = flow {
        if (articleId.isEmpty()) {
            emit(NewsResult.Error("No Data"))
            return@flow
        }

        articleDao.getArticleById(articleId)?.let { article ->
            emit(NewsResult.Success(article.toArticle()))
            return@flow
        }

        if (!networkMonitor.isCurrentlyOnline()) {
            emit(NewsResult.Error("No Data"))
            return@flow
        }

        try {
            val language = languageRepository.getLanguage().toApiCode()
            val remoteArticle: NewsListDto = httpClient.get(BASE_URL) {
                parameter("apikey", BuildConfig.API_KEY)
                parameter("id", articleId)
                parameter("language", language)
            }.body()

            if (remoteArticle.results?.isNotEmpty() == true) {
                val article = remoteArticle.results[0].toArticle()
                articleDao.upsertArticle(
                    listOf(article.toArticleEntity())
                )
                emit(NewsResult.Success(article))
            } else {
                emit(NewsResult.Error("No Data"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            emit(NewsResult.Error("No Data"))
        }
    }

    override suspend fun pagination(nextPage: String?): Flow<NewsResult<NewsList>> = flow {
        if (nextPage.isNullOrBlank()) {
            emit(NewsResult.Error("No next page"))
            return@flow
        }

        if (!networkMonitor.isCurrentlyOnline()) {
            emit(NewsResult.Error("Offline"))
            return@flow
        }

        val language = languageRepository.getLanguage().toApiCode()
        val remoteNewsList = try {
            getRemote(nextPage, language)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        remoteNewsList?.let {
            articleDao.upsertArticle(
                it.articles.map { article -> article.toArticleEntity() }
            )
            emit(NewsResult.Success(it))
            return@flow
        }
        emit(NewsResult.Error("Could not load next page"))
    }

    private fun AppLanguage.toApiCode(): String = when (this) {
        AppLanguage.ENGLISH -> "en"
        AppLanguage.PERSIAN -> "fa"
    }
}