/**
 * Назначение: Models.kt
 * Дата создания: 10.02.2026
 * Автор: Валерьева Татьяна
 * Описание: Модели данных для упражнений, таблицы лидеров и сущность пользователя для БД.
 */
package com.example.mytest.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. Модель для БД (UserEntity) - НУЖНА ДЛЯ AppDatabase
@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val id: Int = 0,
    val firstName: String,
    val email: String,
    val token: String? = null
)

// 2. Модель для списка упражнений - НУЖНА ДЛЯ ExerciseAdapter
data class Exercise(
    val id: Int,
    val title: String,
    val description: String,
    val icon: String
)

// 3. Модель для таблицы лидеров - НУЖНА ДЛЯ MockData
data class Leader(
    val name: String,
    val score: Int,
    val avatar: String
)