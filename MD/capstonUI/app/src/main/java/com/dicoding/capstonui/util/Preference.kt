package com.dicoding.capstonui.utils

import android.content.Context
import android.content.SharedPreferences

object Preference {


    private const val PREFS_NAME = "prefs"
    private const val KEY_TOKEN = "token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getPreferences(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String {
        return getPreferences(context).getString(KEY_TOKEN, "") ?: ""
    }

    fun clearToken(context: Context) {
        getPreferences(context).edit().remove(KEY_TOKEN).apply()
    }
}
