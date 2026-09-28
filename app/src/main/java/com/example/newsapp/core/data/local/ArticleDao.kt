package com.example.newsapp.core.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ArticleDao {

    @Upsert
    suspend fun upsertArticle(articleList: List<ArticleEntity>)

    @Query("SELECT * FROM articleentity")
    suspend fun getArticleList(): List<ArticleEntity>

    @Query("SELECT * FROM articleentity WHERE articleId = :articleId")
    suspend fun getArticleById(articleId: String): ArticleEntity?

    @Query("DELETE FROM articleentity")
    suspend fun clearDatabase()
}