# Модуль Ж: Тестирование (TDD)

## Требование из регламента
TDD для Onboarding: RED → GREEN → REFACTOR, 3 коммита.

## Онбординг: логика
- Очередь картинок/текстов
- Очередь создаётся один раз
- Прогресс сохраняется
- Последний элемент → кнопка меняется на "Choose language"

## Шаг 1: RED (написать тесты)
```kotlin
class OnboardingQueueTest {
    private lateinit var queue: OnboardingQueue
    
    @Before
    fun setUp() {
        val data = listOf(
            OnboardingItem("title1", "desc1"),
            OnboardingItem("title2", "desc2"),
            OnboardingItem("title3", "desc3")
        )
        queue = OnboardingQueue(data)
    }
    
    @Test
    fun testFirstElementExtracted() {
        val first = queue.next()
        assertEquals("title1", first.title)
    }
    
    @Test
    fun testQueueSizeDecreases() {
        val sizeBefore = queue.size()
        queue.next()
        val sizeAfter = queue.size()
        assertEquals(sizeBefore - 1, sizeAfter)
    }
    
    @Test
    fun testButtonTextChangesOnLast() {
        // Пройти до последнего элемента
        repeat(2) { queue.next() }
        assertTrue(queue.isLast())
    }
    
    @Test
    fun testEmptyQueueCheck() {
        repeat(3) { queue.next() }
        assertTrue(queue.isEmpty())
    }
}
```

**Сделать коммит:**
```bash
git add .
git commit -m "RED: Onboarding queue tests"
```

## Шаг 2: GREEN (реализовать логику)
```kotlin
data class OnboardingItem(val title: String, val description: String)

class OnboardingQueue(private val items: List<OnboardingItem>) {
    private val queue = items.toMutableList()
    
    fun next(): OnboardingItem {
        if (queue.isEmpty()) throw Exception("Queue is empty")
        return queue.removeAt(0)
    }
    
    fun size(): Int = queue.size
    
    fun isEmpty(): Boolean = queue.isEmpty()
    
    fun isLast(): Boolean = queue.size == 1
    
    fun peek(): OnboardingItem? = queue.firstOrNull()
}

// Activity
class OnboardingActivity : AppCompatActivity() {
    private lateinit var queue: OnboardingQueue
    private var hasSeenOnboarding = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val prefs = getSharedPreferences("onboarding", MODE_PRIVATE)
        hasSeenOnboarding = prefs.getBoolean("has_seen", false)
        
        if (!hasSeenOnboarding) {
            queue = OnboardingQueue(createOnboardingData())
            showCurrent()
        } else {
            startActivity(Intent(this, LanguageActivity::class.java))
            finish()
        }
    }
    
    private fun showCurrent() {
        val current = queue.peek() ?: return
        binding.tvTitle.text = current.title
        binding.tvDescription.text = current.description
        
        if (queue.isLast()) {
            binding.btnNext.text = "Choose language"
        } else {
            binding.btnNext.text = "Next"
        }
    }
    
    private fun onNextClick() {
        queue.next()
        
        if (queue.isEmpty()) {
            markOnboardingComplete()
            startActivity(Intent(this, LanguageActivity::class.java))
            finish()
        } else {
            showCurrent()
        }
    }
    
    private fun markOnboardingComplete() {
        val prefs = getSharedPreferences("onboarding", MODE_PRIVATE)
        prefs.edit().putBoolean("has_seen", true).apply()
    }
    
    private fun createOnboardingData(): List<OnboardingItem> {
        return listOf(
            OnboardingItem("Welcome", "This is an educational app"),
            OnboardingItem("Learn", "Practice your language skills"),
            OnboardingItem("Progress", "Track your improvement")
        )
    }
}
```

**Сделать коммит:**
```bash
git add .
git commit -m "GREEN: Onboarding queue implementation"
```

## Шаг 3: REFACTOR (улучшить код)
```kotlin
// 1. Вынести OnboardingData в отдельный объект
object OnboardingDataProvider {
    fun getOnboardingItems(): List<OnboardingItem> {
        return listOf(
            OnboardingItem("Welcome", "This is an educational app"),
            OnboardingItem("Learn", "Practice your language skills"),
            OnboardingItem("Progress", "Track your improvement")
        )
    }
}

// 2. Создать интерфейс для Queue
interface IOnboardingQueue {
    fun next(): OnboardingItem
    fun size(): Int
    fun isEmpty(): Boolean
    fun isLast(): Boolean
}

class OnboardingQueue(items: List<OnboardingItem>) : IOnboardingQueue {
    private val queue = items.toMutableList()
    
    override fun next(): OnboardingItem {
        if (queue.isEmpty()) throw Exception("Queue is empty")
        return queue.removeAt(0)
    }
    
    override fun size(): Int = queue.size
    override fun isEmpty(): Boolean = queue.isEmpty()
    override fun isLast(): Boolean = queue.size == 1
}

// 3. Упростить Activity
class OnboardingActivity : AppCompatActivity() {
    private lateinit var queue: IOnboardingQueue
    private val prefs by lazy { getSharedPreferences("onboarding", MODE_PRIVATE) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        if (prefs.getBoolean("has_seen", false)) {
            navigateToLanguage()
            return
        }
        
        initializeOnboarding()
    }
    
    private fun initializeOnboarding() {
        queue = OnboardingQueue(OnboardingDataProvider.getOnboardingItems())
        setupUI()
    }
    
    private fun setupUI() {
        val current = queue.next()
        binding.apply {
            tvTitle.text = current.title
            tvDescription.text = current.description
            btnNext.text = if (queue.isLast()) "Choose language" else "Next"
            btnNext.setOnClickListener { onNext() }
        }
    }
    
    private fun onNext() {
        if (queue.isEmpty()) {
            completeOnboarding()
        } else {
            setupUI()
        }
    }
    
    private fun completeOnboarding() {
        prefs.edit().putBoolean("has_seen", true).apply()
        navigateToLanguage()
    }
    
    private fun navigateToLanguage() {
        startActivity(Intent(this, LanguageActivity::class.java))
        finish()
    }
}
```

**Сделать коммит:**
```bash
git add .
git commit -m "REFACTOR: Onboarding improvements"
```

---

Следующее: [Модуль З: Публикация](G-delivery.md)
