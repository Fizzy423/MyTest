/**
 * Назначение: Database.kt
 * Дата создания: 10.02.2026
 * Автор: Валерьева Татьяна
 * Описание: Интерфейс DAO (Data Access Object) для выполнения запросов к таблице пользователей в базе данных Room.
 */
package com.example.mytest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mytest.domain.UserEntity

@Dao
interface UserDao {

    /**
     * Сохранение или обновление данных пользователя.
     * Используется OnConflictStrategy.REPLACE для автоматического обновления профиля.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Получение данных текущего авторизованного пользователя.
     * Возвращает null, если пользователь не найден (не авторизован).
     */
    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun getUser(): UserEntity?

    /**
     * Удаление всех данных пользователя из локальной базы (выход из аккаунта).
     */
    @Query("DELETE FROM user_table")
    suspend fun clearTable()
}