/**
 * Назначение: AppDatabase.kt
 * Дата создания: 10.02.2026
 * Автор: Валерьева Татьяна
 * Описание: Основной класс базы данных Room для хранения данных пользователя.
 */
package com.example.mytest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mytest.domain.UserEntity // Импортируем нашу сущность

// Указываю UserEntity вместо PhotoEntity
@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Метод должен возвращать UserDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Полезно на конкурсе: при смене версии БД не вылетит
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}