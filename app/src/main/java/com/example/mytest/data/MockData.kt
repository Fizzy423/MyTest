package com.example.mytest.data

import com.example.mytest.domain.Product

object MockData {
    val fakeProducts = listOf(
        Product(1, "Товар 1", "100 руб", "https://via.placeholder.com/150"),
        Product(2, "Товар 2", "200 руб", "https://via.placeholder.com/150"),
        Product(3, "Товар 3", "300 руб", "https://via.placeholder.com/150")
    )
}