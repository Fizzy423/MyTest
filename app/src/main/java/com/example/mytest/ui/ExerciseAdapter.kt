/**
 * Назначение: ExerciseAdapter.kt
 * Дата создания: 10.02.2026
 * Автор: Валерьева Татьяна
 * Описание: Адаптер для отображения плитки упражнений на главном экране.
 */
package com.example.mytest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytest.databinding.ItemExerciseBinding // Студия сама подтянет после создания XML
import com.example.mytest.domain.Exercise

class ExerciseAdapter : RecyclerView.Adapter<ExerciseAdapter.Holder>() {

    var items: List<Exercise> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // В конструктор Holder передаем именно ItemExerciseBinding
    class Holder(val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // Здесь мы раздуваем нашу новую разметку
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            // Теперь эти ID (tvTitle, tvDescription) существуют в XML и будут видны здесь
            tvTitle.text = item.title
            tvDescription.text = item.description
        }
    }

    override fun getItemCount() = items.size
}