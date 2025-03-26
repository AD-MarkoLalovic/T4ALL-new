package com.mobility.enp.util

import android.content.Context
import androidx.core.content.edit


object SharedPreferencesHelper {

    private const val PREFS_NAME = "AppPreferences"

    private fun getPreferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Funkcija za promenu jezika na aplikaciji
    fun setUserLanguage(context: Context, language: String) {
        getPreferences(context).edit {
            putString("user_language", language)
        }
    }

    // Funkzija za dobijanje
    fun getUserLanguage(context: Context): String {
        return getPreferences(context).getString("user_language", "sr") ?: "sr"
    }

    // Funkcija za prikazivanje odabranog jezika na intro ekranima
    fun getSaveIntroSelectedLanguage(context: Context): String {
        return getPreferences(context).getString("selected_Language", "sr") ?: "sr"
    }

    // Funkcija za postavljanje jezika na intro ekranima
    fun setSaveIntroSelectedLanguage(context: Context, language: String) {
        getPreferences(context).edit {
            putString("selected_Language", language)
        }
    }

    // Funkcija za proveru da li je aplikacija prvi put pokrenuta
    fun isFirstLaunch(context: Context): Boolean {
        return getPreferences(context).getBoolean("isFirstLaunch", true)
    }

    // Funkcija za postavljanje da aplikacija nije prvi put pokrenuta
    fun setFirstLaunch(context: Context, isFirstLaunch: Boolean) {
        getPreferences(context).edit {
            putBoolean("isFirstLaunch", isFirstLaunch)
        }
    }

    //Funkcija za setovanje jezika app za poruku o promeni jezika u odgovarajucem jeziku aplikacije
    fun setLanguageChanged(context: Context, languageChanged: Boolean) {
        getPreferences(context).edit {
            putBoolean("language_changed", languageChanged)
        }
    }

    // Funkcija za preuzimanje jezika app za poruku o promeni jezika u odgovarajucem jeziku aplikacije
    fun getLanguageChanged(context: Context): Boolean {
        return getPreferences(context).getBoolean("language_changed", false)
    }

}


