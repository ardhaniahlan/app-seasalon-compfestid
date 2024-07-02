package org.apps.salon

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "LoginPrefs"
        private const val IS_LOGGED_IN = "isLoggedIn"
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        with(preferences.edit()) {
            putBoolean(IS_LOGGED_IN, isLoggedIn)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(IS_LOGGED_IN, false)
    }
}