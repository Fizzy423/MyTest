# 📜 RecyclerView и адаптеры

## 🎯 Что такое RecyclerView?

RecyclerView - это эффективный способ отображения списков. Он переиспользует Views, что экономит память.

## 🏗️ Структура RecyclerView

1. **RecyclerView** - сам список в XML
2. **Adapter** - связывает данные с Views
3. **ViewHolder** - хранит ссылки на Views одного элемента
4. **LayoutManager** - управляет расположением элементов
5. **Item Layout** - XML разметка одного элемента

## 📝 Пошаговая реализация

### Шаг 1: Модель данных

```kotlin
// domain/Models.kt
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val imageUrl: String
)
```

### Шаг 2: Layout элемента списка

```xml
<!-- res/layout/item_product.xml -->
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp">

        <ImageView
            android:id="@+id/ivProductImage"
            android:layout_width="80dp"
            android:layout_height="80dp"
            android:scaleType="centerCrop" />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:layout_marginStart="16dp">

            <TextView
                android:id="@+id/tvProductName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Название товара"
                android:textSize="18sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvProductPrice"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="1000 ₽"
                android:textSize="16sp"
                android:layout_marginTop="8dp"
                android:textColor="@color/primary" />

        </LinearLayout>

    </LinearLayout>

</androidx.cardview.widget.CardView>
```

### Шаг 3: Адаптер

```kotlin
// ui/ProductAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.databinding.ItemProductBinding
import com.bumptech.glide.Glide

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Список данных
    var items: List<Product> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()  // Обновить список
        }

    // Слушатель клика
    var onItemClick: ((Product) -> Unit)? = null

    // ViewHolder - хранит ссылки на Views
    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            // Заполнить данными
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "${product.price} ₽"
            
            // Загрузить изображение
            Glide.with(binding.ivProductImage)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(binding.ivProductImage)

            // Клик на элемент
            binding.root.setOnClickListener {
                onItemClick?.invoke(product)
            }
        }
    }

    // Создать ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    // Привязать данные к ViewHolder
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // Количество элементов
    override fun getItemCount(): Int = items.size
}
```

### Шаг 4: RecyclerView в Activity

```xml
<!-- activity_main.xml -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rvProducts"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="8dp" />
```

```kotlin
// MainActivity.kt
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        // Настроить RecyclerView
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter

        // Обработать клик
        adapter.onItemClick = { product ->
            Toast.makeText(this, "Выбран: ${product.name}", Toast.LENGTH_SHORT).show()
            // Открыть детальную информацию
            openProductDetails(product)
        }
    }

    private fun loadData() {
        // Загрузить данные (пока заглушка)
        val products = listOf(
            Product(1, "Телефон Samsung", 50000.0, "https://..."),
            Product(2, "Ноутбук Asus", 80000.0, "https://..."),
            Product(3, "Наушники Sony", 5000.0, "https://...")
        )
        adapter.items = products
    }

    private fun openProductDetails(product: Product) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("PRODUCT_ID", product.id)
        startActivity(intent)
    }
}
```

## 🔄 DiffUtil - Умное обновление

```kotlin
import androidx.recyclerview.widget.DiffUtil

class ProductDiffCallback(
    private val oldList: List<Product>,
    private val newList: List<Product>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

// В адаптере
class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var items: List<Product> = emptyList()

    fun updateItems(newItems: List<Product>) {
        val diffCallback = ProductDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        items = newItems
        diffResult.dispatchUpdatesTo(this)  // Анимированное обновление
    }

    // ...остальной код
}
```

## 📱 Типы LayoutManager

### LinearLayoutManager (список)

```kotlin
// Вертикальный список (по умолчанию)
binding.rvProducts.layoutManager = LinearLayoutManager(this)

// Горизонтальный список
binding.rvProducts.layoutManager = LinearLayoutManager(
    this,
    LinearLayoutManager.HORIZONTAL,
    false
)

// Обратный порядок
binding.rvProducts.layoutManager = LinearLayoutManager(
    this,
    LinearLayoutManager.VERTICAL,
    true  // reverseLayout
)
```

### GridLayoutManager (сетка)

```kotlin
// 2 колонки
binding.rvProducts.layoutManager = GridLayoutManager(this, 2)

// 3 колонки горизонтально
binding.rvProducts.layoutManager = GridLayoutManager(
    this,
    3,
    GridLayoutManager.HORIZONTAL,
    false
)
```

### StaggeredGridLayoutManager (Pinterest стиль)

```kotlin
binding.rvProducts.layoutManager = StaggeredGridLayoutManager(
    2,  // колонки
    StaggeredGridLayoutManager.VERTICAL
)
```

## 🎨 Декораторы (ItemDecoration)

### Разделители между элементами

```kotlin
import androidx.recyclerview.widget.DividerItemDecoration

// Добавить разделители
val divider = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
binding.rvProducts.addItemDecoration(divider)
```

### Отступы вокруг элементов

```kotlin
class SpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.top = spacing
        outRect.bottom = spacing
    }
}

// Использование
binding.rvProducts.addItemDecoration(SpacingItemDecoration(16))
```

## 🔍 Множественные типы элементов

```kotlin
class MultiTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
        const val TYPE_FOOTER = 2
    }

    var items: List<Any> = emptyList()

    // Определить тип элемента
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Header -> TYPE_HEADER
            is Product -> TYPE_ITEM
            is Footer -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }

    // Создать ViewHolder в зависимости от типа
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            TYPE_ITEM -> {
                val binding = ItemProductBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ProductViewHolder(binding)
            }
            TYPE_FOOTER -> {
                val binding = ItemFooterBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                FooterViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(items[position] as Header)
            is ProductViewHolder -> holder.bind(items[position] as Product)
            is FooterViewHolder -> holder.bind(items[position] as Footer)
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolders
    class HeaderViewHolder(private val binding: ItemHeaderBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: Header) { /* ... */ }
    }

    class ProductViewHolder(private val binding: ItemProductBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) { /* ... */ }
    }

    class FooterViewHolder(private val binding: ItemFooterBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(footer: Footer) { /* ... */ }
    }
}
```

## 📋 ListAdapter (упрощенный подход)

```kotlin
import androidx.recyclerview.widget.ListAdapter

class ProductListAdapter : ListAdapter<Product, ProductListAdapter.ViewHolder>(ProductComparator) {

    // Comparator для DiffUtil
    object ProductComparator : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    var onItemClick: ((Product) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemProductBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "${product.price} ₽"
            
            binding.root.setOnClickListener {
                onItemClick?.invoke(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))  // getItem из ListAdapter
    }
}

// Использование
val adapter = ProductListAdapter()
binding.rvProducts.adapter = adapter

// Обновление списка (с анимацией автоматически)
adapter.submitList(newProducts)
```

## 🎯 Частые паттерны

### Пустой список

```kotlin
fun updateList(items: List<Product>) {
    if (items.isEmpty()) {
        binding.rvProducts.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
        binding.tvEmpty.text = "Список пуст"
    } else {
        binding.rvProducts.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        adapter.items = items
    }
}
```

### Загрузка данных

```kotlin
private fun loadData() {
    binding.progressBar.visibility = View.VISIBLE
    binding.rvProducts.visibility = View.GONE
    
    // Загрузка... (например, из сети)
    lifecycleScope.launch {
        try {
            val products = repository.getProducts()
            adapter.items = products
            binding.rvProducts.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            binding.progressBar.visibility = View.GONE
        }
    }
}
```

### Бесконечный скролл (pagination)

```kotlin
binding.rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount
        
        // Если почти докрутили до конца
        if (lastVisiblePosition >= totalItemCount - 5 && !isLoading) {
            loadMoreData()
        }
    }
})
```

---

**Совет**: Используй ListAdapter для простых случаев, обычный Adapter для сложных!
