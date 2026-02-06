package com.example.mytest.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

// Для списка товаров
data class Product(
    val id: Int,
    val title: String,
    val price: String,
    val image: String
)

// Для интернета
data class PhotoDto(
    val id: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)

// Для базы данных
@Entity(tableName = "photos_table")
data class PhotoEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val url: String
)