package com.example.mytest.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mytest.data.MockData
import com.example.mytest.databinding.ActivityMainBinding

/**
 * Автор: Валерьева Татьяна
 * Дата: 10.02.2026
 * Назначение: Главный экран со списком упражнений
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val exerciseAdapter = ExerciseAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация списка
        setupRecyclerView()

        // Загрузка данных
        loadData()
    }

    private fun setupRecyclerView() {
        // rvMain - переименуй id в activity_main.xml
        binding.rvMain.adapter = exerciseAdapter
    }

    private fun loadData() {
        // Используем наши новые MockData
        exerciseAdapter.items = MockData.exercises
    }
}