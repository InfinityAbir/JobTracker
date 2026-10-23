package com.example.util

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "En"),
    BANGLA("bn", "Bn")
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

object LanguageManager {
    private const val PREFS_NAME = "job_tracker_prefs"
    private const val KEY_LANGUAGE = "app_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code)
        _currentLanguage.value = if (saved == AppLanguage.BANGLA.code) AppLanguage.BANGLA else AppLanguage.ENGLISH
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _currentLanguage.value = language
    }

    fun toggleLanguage(context: Context) {
        val next = if (_currentLanguage.value == AppLanguage.ENGLISH) AppLanguage.BANGLA else AppLanguage.ENGLISH
        setLanguage(context, next)
    }
}
