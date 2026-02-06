package com.example.mytest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mytest.data.MockData
import com.example.mytest.databinding.ActivityMainBinding
import com.example.mytest.ui.ProductAdapter

class MainActivity : AppCompatActivity() {

    // Настройка ViewBinding
    private lateinit var binding: ActivityMainBinding
    private val adapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Привязываем адаптер к списку в XML
        binding.rvProducts.adapter = adapter

        // 2. Загружаем данные (пока из наших заглушек MockData)
        // На конкурсе здесь будет вызов из Интернета или Базы
        adapter.items = MockData.fakeProducts
    }
}