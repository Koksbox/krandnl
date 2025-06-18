// Плагины для Android приложения
plugins {
    alias(libs.plugins.android.application) // Плагин для Android приложений
    alias(libs.plugins.kotlin.android) // Плагин для Kotlin
}

// Конфигурация Android проекта
android {
    namespace = "com.example.myapplication" // Уникальный идентификатор пакета
    compileSdk = 35 // Версия SDK для компиляции

    // Основная конфигурация приложения
    defaultConfig {
        applicationId = "com.example.myapplication" // ID приложения в Google Play
        minSdk = 30 // Минимальная версия Android (Android 11)
        targetSdk = 35 // Целевая версия Android
        versionCode = 1 // Версия кода для обновлений
        versionName = "1.0" // Пользовательская версия

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // Тестовый раннер
    }

    // Конфигурация типов сборки
    buildTypes {
        release {
            isMinifyEnabled = false // Отключение минификации кода
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // Стандартные правила ProGuard
                "proguard-rules.pro" // Пользовательские правила ProGuard
            )
        }
    }
    
    // Настройки компиляции Java
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Совместимость исходного кода
        targetCompatibility = JavaVersion.VERSION_11 // Целевая совместимость
    }
    
    // Настройки Kotlin
    kotlinOptions {
        jvmTarget = "11" // Целевая версия JVM
    }
}

// Зависимости проекта
dependencies {
    // Основные Android библиотеки
    implementation(libs.androidx.core.ktx) // Kotlin расширения для Android
    implementation(libs.androidx.appcompat) // Совместимость с AppCompat
    implementation(libs.material) // Material Design компоненты
    
    // Тестовые зависимости
    testImplementation(libs.junit) // JUnit для unit тестов
    androidTestImplementation(libs.androidx.junit) // JUnit для Android тестов
    androidTestImplementation(libs.androidx.espresso.core) // Espresso для UI тестов
}