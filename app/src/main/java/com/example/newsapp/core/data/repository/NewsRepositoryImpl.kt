package com.example.newsapp.core.data.repository

import com.example.newsapp.BuildConfig
import com.example.newsapp.core.data.Config.BASE_URL
import com.example.newsapp.core.data.local.ArticleDao
import com.example.newsapp.core.data.remote.NewsListDto
import com.example.newsapp.core.data.toArticle
import com.example.newsapp.core.data.toArticleEntity
import com.example.newsapp.core.data.toNewsList
import com.example.newsapp.core.domain.Article
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
    private val articleDao: ArticleDao
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
        val localNews = articleDao.getArticleList()
        return NewsList(
            nextPage = nextPage,
            articles = localNews.map { it.toArticle() }
        )
    }

    override suspend fun getNews(language: String): Flow<NewsResult<NewsList>> {
        return flow {
            val remoteNewsList = try {
                getRemote(null,language)
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                null
            }
            remoteNewsList?.let {
                articleDao.clearDatabase()
                articleDao.upsertArticle(remoteNewsList.articles.map { it.toArticleEntity() })
                emit(NewsResult.Success(getLocal(nextPage = remoteNewsList.nextPage)))
                return@flow
            }

            val localNewsList = getLocal(null)
            if (localNewsList.articles.isNotEmpty()) {
                emit(NewsResult.Success(localNewsList))
                return@flow
            }
            emit(NewsResult.Error("No Data"))
        }
    }

    override suspend fun getArticle(articleId: String): Flow<NewsResult<Article>> {
        return flow {
            articleDao.getArticleById(articleId)?.let { article ->
                emit(NewsResult.Success(article.toArticle()))
                return@flow
            }

            try {
                val remoteArticle: NewsListDto = httpClient.get(BASE_URL) {
                    parameter("apikey", BuildConfig.API_KEY)
                    parameter("id", articleId)
                }.body()
                if (remoteArticle.results?.isNotEmpty() == true) {
                    emit(NewsResult.Success(data = remoteArticle.results[0].toArticle()))
                } else {
                    emit(NewsResult.Error("No Data"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                emit(NewsResult.Error("No Data"))
            }
        }
    }

    override suspend fun pagination(nextPage: String?,language: String): Flow<NewsResult<NewsList>> {
        return flow {
            val remoteNewsList = try {
                getRemote(nextPage,language)
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                null
            }
            remoteNewsList?.let {
                articleDao.upsertArticle(remoteNewsList.articles.map { it.toArticleEntity() })
                emit(NewsResult.Success(remoteNewsList))
                return@flow
            }
        }
    }
}