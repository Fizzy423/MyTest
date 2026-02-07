# Модуль Е: Аппаратные функции

## Требование из регламента
Камера, галерея, обрезка фото, смена темы, уведомления, TensorFlow, AR, виджет, микрофон.

## Камера и галерея
```kotlin
val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
    if (uri != null) {
        binding.ivProfile.setImageURI(uri)
        // Переход на экран обрезки
        startActivity(Intent(this, ResizePhotoActivity::class.java).apply {
            putExtra("image_uri", uri.toString())
        })
    }
}

// На кнопку "выбрать фото"
binding.btnChangeImage.setOnClickListener {
    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
}
```

## Обрезка фото (квадрат с круглой областью)
```kotlin
class ResizePhotoActivity : AppCompatActivity() {
    private var currentX = 0f
    private var currentY = 0f
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val imageUri = intent.getStringExtra("image_uri")
        Glide.with(this).load(imageUri).into(binding.ivImage)
        
        // Позволить пересдвигать квадрат
        binding.ivImage.setOnTouchListener { _, event ->
            currentX = event.x
            currentY = event.y
            binding.ivImage.invalidate()
            true
        }
        
        // Рисование круглой области
        binding.ivImage.setOnDrawListener {
            drawCircleOverlay()
        }
        
        // Сохранить обрезанное фото
        binding.btnUseImage.setOnClickListener {
            saveCroppedImage()
        }
    }
    
    private fun drawCircleOverlay() {
        // Нарисовать полупрозрачный фон и круговую область без заливки
        val canvas = Canvas()
        val paint = Paint()
        paint.color = Color.argb(200, 0, 0, 0)  // Полупрозрачный черный
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Круг без заливки
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.WHITE
        canvas.drawCircle(currentX, currentY, 150f, paint)
    }
    
    private fun saveCroppedImage() {
        // Вырезать круговую область и загрузить на сервер
        val cropped = cropImage(currentX, currentY, 150)
        uploadAvatar(cropped)
    }
}
```

## Смена темы (светлая/темная)
```kotlin
object ThemeManager {
    fun setDarkMode(context: Context, isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        
        // Сохранить в SharedPreferences
        val prefs = context.getSharedPreferences("app", MODE_PRIVATE)
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }
    
    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app", MODE_PRIVATE)
        return prefs.getBoolean("dark_mode", false)
    }
}

// В Profile Activity
var isDarkMode = ThemeManager.isDarkMode(this)
binding.btnTheme.text = if (isDarkMode) "Light Theme" else "Dark Theme"
binding.btnTheme.setOnClickListener {
    isDarkMode = !isDarkMode
    binding.btnTheme.text = if (isDarkMode) "Light Theme" else "Dark Theme"
    ThemeManager.setDarkMode(this, isDarkMode)
}
```

## Локальное уведомление (1 раз за сессию)
```kotlin
class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("app", Context.MODE_PRIVATE)
        val alreadySent = prefs.getBoolean("notification_sent", false)
        
        if (!alreadySent) {
            showNotification()
            prefs.edit().putBoolean("notification_sent", true).apply()
        }
        
        return Result.success()
    }
    
    private fun showNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "channel_id")
            .setContentTitle("Welcome back!")
            .setContentText("We missed you")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1, notification)
    }
}

// Запустить в onStop Activity
override fun onStop() {
    super.onStop()
    val request = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(1, TimeUnit.MINUTES)
        .addTag("notification")
        .build()
    
    WorkManager.getInstance(this)
        .enqueueUniqueWork("notify_once", ExistingWorkPolicy.KEEP, request)
}

// Очистить при входе
override fun onResume() {
    super.onResume()
    val prefs = getSharedPreferences("app", MODE_PRIVATE)
    prefs.edit().putBoolean("notification_sent", false).apply()
}
```

## TensorFlow Lite (распознавание животных)
```kotlin
class TensorFlowHelper(context: Context) {
    private val interpreter = Interpreter(loadModel(context))
    
    private fun loadModel(context: Context): MappedByteBuffer {
        val assetManager = context.assets
        val modelPath = "animals_model.tflite"
        return assetManager.openFd(modelPath).use { fileDescriptor ->
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            inputStream.channel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }
    
    fun recognize(bitmap: Bitmap): String {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }
        
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val pixel = resized.getPixel(i, j)
                input[0][i][j][0] = (pixel shr 16 and 0xFF) / 255f
                input[0][i][j][1] = (pixel shr 8 and 0xFF) / 255f
                input[0][i][j][2] = (pixel and 0xFF) / 255f
            }
        }
        
        val output = Array(1) { FloatArray(1001) }
        interpreter.run(input, output)
        
        val maxIndex = output[0].withIndex().maxByOrNull { it.value }?.index ?: 0
        return getAnimalName(maxIndex)
    }
    
    private fun getAnimalName(index: Int): String {
        val animals = listOf("cat", "dog", "lion", "tiger", "bear", /* ... */ )
        return if (index < animals.size) animals[index] else "unknown"
    }
}
```

## Виджет (позиция в рейтинге)
```kotlin
class RankingWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_ranking)
            
            val isLoggedIn = AuthManager.isLoggedIn(context)
            
            if (!isLoggedIn) {
                views.setTextViewText(R.id.tvRank, "Sign in to see your rank")
            } else {
                val rank = getRankingPosition(context)
                val neighbors = getNeighbors(context, rank)  // 1 выше, 2 ниже
                
                views.setTextViewText(R.id.tvYourRank, "Your rank: #$rank")
                views.setTextViewText(R.id.tvNeighbors, neighbors)
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
```

## Микрофон + распознавание речи (Аудирование)
```kotlin
class ListeningExerciseActivity : AppCompatActivity() {
    private var isRecording = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListeningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnMicrophone.setOnClickListener {
            if (!isRecording) {
                startRecording()
            } else {
                stopRecording()
            }
        }
    }
    
    private fun startRecording() {
        isRecording = true
        binding.btnMicrophone.startPulsing()  // Пульсирующая анимация
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
        
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val recognizedText = results?.get(0) ?: ""
                
                checkAnswer(recognizedText)
            }
            stopRecording()
        }
        
        launcher.launch(intent)
    }
    
    private fun stopRecording() {
        isRecording = false
        binding.btnMicrophone.stopPulsing()
    }
    
    private fun checkAnswer(spokenWord: String) {
        val expectedWord = binding.tvWord.text.toString()
        if (spokenWord.lowercase() == expectedWord.lowercase()) {
            showSuccess()
            addPoints(1 + 2 * getStreak())
        } else {
            showError(spokenWord)
            resetStreak()
        }
    }
}

// Пульсирующая кнопка
fun View.startPulsing() {
    val pulse = ObjectAnimator.ofPropertyValuesHolder(
        this,
        PropertyValuesHolder.ofFloat("scaleX", 1.2f),
        PropertyValuesHolder.ofFloat("scaleY", 1.2f)
    ).apply {
        duration = 500
        repeatCount = ObjectAnimator.INFINITE
        repeatMode = ObjectAnimator.REVERSE
    }
    pulse.start()
}
```

---

Следующее: [Модуль Ж: Подготовка](F-testing.md) и [Модуль З: Публикация](G-delivery.md)
