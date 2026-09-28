package com.example.newsapp.core.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.newsapp.article.presentation.ArticleScreenCore
import com.example.newsapp.news.presentation.NewsScreenCore

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.News) {
        composable<Screens.News> {
            NewsScreenCore {
                navController.navigate(Screens.Article(it))
            }
        }
        composable<Screens.Article> {
            val article: Screens.Article = it.toRoute()
            ArticleScreenCore(articleId = article.articleId) {
                navController.navigate(Screens.News)
            }
        }
    }
}