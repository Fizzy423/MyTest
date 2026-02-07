# Готовые шаблоны кода

Копируй эти шаблоны и адаптируй под свой проект.

## Шаблон: ViewModel с Repository

```kotlin
class ExerciseViewModel(private val repo: ExerciseRepository) : ViewModel() {
    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises
    
    private val _currentExercise = MutableLiveData<Exercise>()
    val currentExercise: LiveData<Exercise> = _currentExercise
    
    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score
    
    fun loadExercises() {
        viewModelScope.launch {
            try {
                val data = repo.getExercises()
                _exercises.value = data
            } catch (e: Exception) {
                Log.e("VM", e.message ?: "Error")
            }
        }
    }
    
    fun submitAnswer(exerciseId: Int, correct: Boolean) {
        viewModelScope.launch {
            try {
                val points = if (correct) 1 else 0
                _score.value = (_score.value ?: 0) + points
                repo.submitResult(exerciseId, correct)
            } catch (e: Exception) {
                Log.e("VM", e.message ?: "Error")
            }
        }
    }
}
```

## Шаблон: RecyclerView Adapter

```kotlin
class ExerciseAdapter(private val onClick: (Exercise) -> Unit) : 
    RecyclerView.Adapter<ExerciseAdapter.VH>() {
    
    private var items: List<Exercise> = emptyList()
    
    fun setItems(list: List<Exercise>) {
        items = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    inner class VH(private val binding: ItemExerciseBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: Exercise) {
            binding.apply {
                tvName.text = item.name
                tvDescription.text = item.description
                root.setOnClickListener { onClick(item) }
            }
        }
    }
}
```

## Шаблон: Retrofit API Service

```kotlin
interface ApiService {
    @GET("rest/v1/exercises")
    suspend fun getExercises(): List<ExerciseDto>
    
    @POST("rest/v1/results")
    suspend fun submitResult(@Body result: ResultDto): ResponseDto
    
    @PUT("rest/v1/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UserDto): UserDto
    
    @DELETE("rest/v1/results/{id}")
    suspend fun deleteResult(@Path("id") id: Int): ResponseDto
}

object RetrofitClient {
    private const val BASE_URL = "https://your-api.com/"
    
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

## Шаблон: Room Entity и DAO

```kotlin
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val points: Int
)

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity)
    
    @Query("SELECT * FROM exercises")
    fun getAll(): Flow<List<ExerciseEntity>>
    
    @Delete
    suspend fun delete(exercise: ExerciseEntity)
}
```

## Шаблон: Activity с Binding

```kotlin
class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener { finish() }
            tvTitle.text = "Detail"
        }
    }
}
```

## Шаблон: Repository

```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    suspend fun getUsers(): List<User> {
        return try {
            val remote = apiService.getUsers()
            userDao.insertUsers(remote.map { it.toEntity() })
            remote.map { it.toModel() }
        } catch (e: Exception) {
            userDao.getAllUsers().first().map { it.toModel() }
        }
    }
}
```

---

Следующее: [Common Errors](10-common-errors.md)
