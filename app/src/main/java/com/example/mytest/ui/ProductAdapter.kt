package com.example.mytest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytest.databinding.ItemProductBinding
import com.example.mytest.domain.Product

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.Holder>() {
    var items: List<Product> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class Holder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvTitle.text = item.title
            tvPrice.text = item.price
            // Картинку пока не грузим, просто заглушка цвета
        }
    }

    override fun getItemCount() = items.size
}