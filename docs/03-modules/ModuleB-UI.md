# Module B: UI и Верстка (реализация экранов)

## MainActivity (главный экран)
```kotlin
/**
 * Назначение: Главный экран с лидербордом и упражнениями
 * Дата: 07.10.2023
 * Автор: Student
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        
        setupUI()
        setupObservers()
        viewModel.loadUsers()
    }
    
    private fun setupUI() {
        // Лидерборд RecyclerView
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.adapter = LeaderboardAdapter()
        
        // Упражнения GridView
        binding.rvExercises.layoutManager = GridLayoutManager(this, 2)
        binding.rvExercises.adapter = ExerciseAdapter { exercise ->
            navigateToExercise(exercise)
        }
        
        // Профиль
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
    
    private fun setupObservers() {
        viewModel.users.observe(this) { users ->
            binding.tvUserName.text = users.firstOrNull()?.name ?: "User"
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            showError(error)
        }
    }
    
    private fun navigateToExercise(exercise: Exercise) {
        val intent = when (exercise.type) {
            ExerciseType.GUESS_ANIMAL -> Intent(this, GuessAnimalActivity::class.java)
            ExerciseType.CHOOSE -> Intent(this, ChooseAnswerActivity::class.java)
            ExerciseType.LISTENING -> Intent(this, ListeningActivity::class.java)
            else -> return
        }
        startActivity(intent)
    }
    
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
```

## LoginActivity
```kotlin
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }
        
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        
        viewModel.loginSuccess.observe(this) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) {
            showError(it)
        }
    }
    
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
```

## SignUpActivity (2 шага)
```kotlin
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private var step = 1  // 1 или 2
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        showStep1()
    }
    
    private fun showStep1() {
        step = 1
        binding.etEmail.visibility = View.VISIBLE
        binding.etPasswordConfirm.visibility = View.GONE
        binding.cbPolicy.visibility = View.GONE
        binding.btnNext.text = "Next"
        
        binding.btnNext.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (isValidEmail(email)) {
                showStep2()
            } else {
                binding.tvError.text = "Invalid email"
            }
        }
    }
    
    private fun showStep2() {
        step = 2
        binding.etPasswordConfirm.visibility = View.VISIBLE
        binding.cbPolicy.visibility = View.VISIBLE
        binding.btnNext.text = "Sign Up"
        
        binding.btnNext.setOnClickListener {
            val password = binding.etPassword.text.toString()
            if (isStrongPassword(password) && binding.cbPolicy.isChecked) {
                signup()
            } else {
                binding.tvError.text = "Weak password or policy not accepted"
            }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.matches("^[a-z0-9]+@[a-z0-9]+\\.[a-z]{2,3}$".toRegex())
    }
    
    private fun isStrongPassword(password: String): Boolean {
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return password.length >= 8 && hasUpper && hasLower && hasDigit && hasSpecial
    }
    
    private fun signup() {
        // API call
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    
    override fun onBackPressed() {
        if (step == 2) {
            showStep1()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
```

## ProfileActivity
```kotlin
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var isDarkMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Смена темы
        isDarkMode = getSharedPreferences("app", MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        
        binding.btnTheme.text = if (isDarkMode) "Light Theme" else "Dark Theme"
        binding.btnTheme.setOnClickListener {
            isDarkMode = !isDarkMode
            binding.btnTheme.text = if (isDarkMode) "Light Theme" else "Dark Theme"
            
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            
            getSharedPreferences("app", MODE_PRIVATE).edit()
                .putBoolean("dark_mode", isDarkMode)
                .apply()
        }
        
        // Выбор фото
        binding.btnChangeImage.setOnClickListener {
            pickImage()
        }
        
        // Выбор языка
        binding.btnLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }
    }
    
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val imageUri = data?.data
            binding.ivProfile.setImageURI(imageUri)
            
            // Переход на экран обрезки
            startActivity(Intent(this, ResizePhotoActivity::class.java).apply {
                putExtra("image_uri", imageUri.toString())
            })
        }
    }
    
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
```

---

Следующее: [Module C: Network](ModuleC-Network.md)
