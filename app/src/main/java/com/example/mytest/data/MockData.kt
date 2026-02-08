/**
 * Назначение: MockData.kt
 * Дата создания: 10.02.2026
 * Автор: Валерьева Татьяна
 * Описание: Заглушки данных для верстки экранов упражнений и таблицы лидеров.
 */

package com.example.mytest.data

import com.example.mytest.domain.Exercise // Импортируем "правильный" класс
import com.example.mytest.domain.Leader   // И лидера тоже

object MockData {
    val exercises = listOf(
        Exercise(1, "Угадай животное", "Развитие навыков распознавания", ""),
        Exercise(2, "Выбери вариант", "Тренировка перевода слов", ""),
        Exercise(3, "Аудирование", "Восприятие речи на слух", "")
    )

    val leaders = listOf(
        Leader("Татьяна", 2500, ""),
        Leader("Иван", 2300, ""),
        Leader("Алексей", 2100, "")
    )
}