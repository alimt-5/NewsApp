package com.example.newsapp.core.di

import androidx.room.Room
import com.example.newsapp.core.data.local.ArticleDatabase
import com.example.newsapp.core.data.local.LanguageRepositoryImpl
import com.example.newsapp.core.data.network.NetworkMonitorImpl
import com.example.newsapp.core.data.repository.NewsRepositoryImpl
import com.example.newsapp.core.domain.LanguageRepository
import com.example.newsapp.core.domain.NetworkMonitor
import com.example.newsapp.core.domain.NewsRepository
import com.example.newsapp.core.domain.usecase.GetArticleUseCase
import com.example.newsapp.core.domain.usecase.GetNextNewsPageUseCase
import com.example.newsapp.core.domain.usecase.GetNewsUseCase
import com.example.newsapp.core.domain.usecase.ObserveLanguageUseCase
import com.example.newsapp.core.domain.usecase.SetLanguageUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            ArticleDatabase::class.java,
            "article.db"
        ).build()
    }

    single {
        get<ArticleDatabase>().dao
    }

    single {
        HttpClient(CIO) {
            expectSuccess = true

            engine {
                endpoint {
                    keepAliveTime = 5000
                    connectTimeout = 5000
                    connectAttempts = 3
                }
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(DefaultRequest) {
                header(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }

                level = LogLevel.ALL
            }
        }
    }

    single<LanguageRepository> {
        LanguageRepositoryImpl(androidApplication())
    }

    single<NetworkMonitor> {
        NetworkMonitorImpl(androidApplication())
    }

    singleOf(::NewsRepositoryImpl).bind<NewsRepository>()

    singleOf(::GetNewsUseCase)
    singleOf(::GetArticleUseCase)
    singleOf(::GetNextNewsPageUseCase)
    singleOf(::ObserveLanguageUseCase)
    singleOf(::SetLanguageUseCase)
}