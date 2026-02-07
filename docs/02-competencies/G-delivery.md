# Модуль З: Подготовка и публикация

## Требование из регламента
Работа в ветке `Path-X`, merge в `main`, презентация, публикация в магазин.

## Git workflow

### Создание и работа в ветке
```bash
# На начало каждой сессии
git checkout -b Path-1     # Сессия 1
git checkout -b Path-2     # Сессия 2
git checkout -b Path-3     # Сессия 3
```

### Коммиты (в течение сессии)
```bash
# Частые коммиты (каждые 30 минут)
git add .
git commit -m "Feature: реализовал Sign Up валидацию"

git add .
git commit -m "Bugfix: исправил тёмную тему"

git add .
git commit -m "Refactor: улучшил ViewModel логику"
```

### В конце сессии (merge в main)
```bash
# Перейти в main
git checkout main

# Слить ветку (она не удалится)
git merge Path-1

# Если есть конфликты — решить их
git status
# ... отредактировать файлы ...
git add .
git commit -m "Merge branch 'Path-1'"

# Отправить на сервер
git push origin main
git push origin Path-1
```

## Презентация для разработчиков

**Содержание (5-10 слайдов):**

1. **Обзор проекта**
   - Название приложения
   - Цель (обучающее приложение для языков)

2. **Архитектура**
   - Диаграмма слоёв (UI → ViewModel → Repository → Data)
   - Используемые паттерны (MVVM, Repository Pattern)

3. **Модули**
   - Какие функции реализованы
   - Какие осталось/не реализовано

4. **Технологии и библиотеки**
   ```
   - Kotlin
   - Android Architecture Components (ViewModel, LiveData)
   - Retrofit (API)
   - Room (БД)
   - Glide (изображения)
   - TensorFlow Lite
   - Coroutines
   ```

5. **Производительность**
   - Размер APK
   - Память (среднее потребление)
   - Скорость загрузки главного экрана

6. **Классовая диаграмма** (ключевые классы)
   ```
   MainActivity → MainViewModel → UserRepository → ApiService
                                               → UserDao (Room)
   ```

7. **Вызовы и интеграция**
   - Как View вызывает ViewModel
   - Как Repository работает с сетью и БД

8. **Проблемы и решения**
   - Какие проблемы были
   - Как их решили

## Публикация в магазин (ruStore/Google Play)

### 1. Подготовка APK
```gradle
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.myapp"
        minSdk 24
        targetSdk 34
    }
    
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

**Сборка:**
```bash
./gradlew assembleRelease
# APK будет в: app/build/outputs/apk/release/app-release.apk
```

### 2. Подпись приложения
```bash
# Создать ключ (один раз)
keytool -genkey -v -keystore my-app.keystore -keyalg RSA -keysize 2048 -validity 10000

# Подписать APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore my-app.keystore \
  app/build/outputs/apk/release/app-release.apk \
  my-key-alias
```

### 3. В магазине (ruStore):

**Создать приложение:**
- Название: `[Фамилия студента]_medic` (латиницей)
- Тип: Образовательное приложение
- Категория: Образование
- Возраст: 12+
- Бесплатное

**Заполнить описания:**
- Краткое: "Мобильное приложение для обучения иностранным языкам"
- Полное: Описать функции и особенности

**Загрузить материалы:**
- Иконка (512x512 PNG)
- Скриншоты (1440x2560 для смартфона)
- Видео-обзор (опционально)

**Добавить аккаунты экспертов:**
- Email или номер аккаунта экспертов
- Роль: Тестер / Администратор

### 4. Отправить на публикацию

---

## Итоговая структура Git

```
main (основная ветка)
├── Path-1 (сессия 1) — архитектура, базовый UI
├── Path-2 (сессия 2) — упражнения, сеть
└── Path-3 (сессия 3) — игра, финальные правки
```

Все ветки остаются в истории, даже после merge.
