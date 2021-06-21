package com.arasthel.navigation.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.arasthel.navigation.screen.Screen
import java.io.Serializable

class ActivityConverter<S: Screen>(
    private val screenClass: Class<S>,
    private val activityClass: Class<out FragmentActivity>,
) {

    companion object {
        const val SCREEN_KEY = "KEY_SCREEN"
        const val SCREEN_CLASS = "KEY_SCREEN_CLASS"
    }

    fun createIntent(context: Context, screen: S): Intent {
        return Intent(context, activityClass).apply {
            configureIntent(this, screen)
        }
    }

    fun configureIntent(intent: Intent, screen: S) {
        val args = intent.extras ?: Bundle()
        when (screen) {
            is Serializable -> args.putSerializable(SCREEN_KEY, screen)
            is Parcelable -> args.putParcelable(SCREEN_KEY, screen)
        }
        args.putString(SCREEN_CLASS, screen::class.java.name)
        intent.putExtras(args)
    }

    @Suppress("UNCHECKED_CAST")
    fun getScreenOrNull(intent: Intent): S? {
        val args = intent.extras
        return when {
            args == null -> null
            Serializable::class.java.isAssignableFrom(screenClass) -> args.getSerializable(
                SCREEN_KEY
            ) as? S
            Parcelable::class.java.isAssignableFrom(screenClass) -> args.getParcelable<Parcelable>(
                SCREEN_KEY
            ) as? S
            else -> throw IllegalArgumentException("Unknown error deserializing Screen")
        }
    }

    fun getScreen(intent: Intent): S {
        return getScreenOrNull(intent)
            ?: throw IllegalArgumentException("Unknown error deserializing Screen. Check if extras are null and Screen is Serializable or Parcelable.")
    }

}