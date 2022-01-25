package com.arasthel.navigation.utils

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.arasthel.navigation.navigators.Navigator
import com.arasthel.navigation.screen.Screen
import java.io.Serializable

class FragmentConverter<S: Screen>(
        private val screenClass: Class<S>,
        private val fragmentClass: Class<out Fragment>,
) {

    companion object {
        const val SCREEN_KEY = "KEY_SCREEN"
        const val SCREEN_CLASS = "KEY_SCREEN_CLASS"
    }

    fun createFragment(screen: S, parentNavigatorId: String?): Fragment {
        return fragmentClass.newInstance().also { configureFragment(it, screen, parentNavigatorId) }
    }

    fun configureFragment(fragment: Fragment, screen: S, parentNavigatorId: String?) {
        val args = fragment.arguments ?: Bundle()
        when (screen) {
            is Serializable -> args.putSerializable(SCREEN_KEY, screen)
            is Parcelable -> args.putParcelable(SCREEN_KEY, screen)
        }
        args.putString(SCREEN_CLASS, screen::class.java.name)
        args.putString(Navigator.PARENT_NAVIGATOR_ID, parentNavigatorId)
        fragment.arguments = args
    }

    fun updateScreen(fragment: Fragment, screen: S) {
        val args = fragment.arguments ?: return
        when (screen) {
            is Serializable -> args.putSerializable(SCREEN_KEY, screen)
            is Parcelable -> args.putParcelable(SCREEN_KEY, screen)
        }
        args.putString(SCREEN_CLASS, screen::class.java.name)
    }

    @Suppress("UNCHECKED_CAST")
    fun getScreenOrNull(fragment: Fragment): S? {
        val args = fragment.arguments
        return when {
            Serializable::class.java.isAssignableFrom(screenClass) -> args?.getSerializable(
                SCREEN_KEY
            ) as? S
            Parcelable::class.java.isAssignableFrom(screenClass) -> args?.getParcelable<Parcelable>(
                SCREEN_KEY
            ) as? S
            else -> null
        }
    }

    fun getScreen(fragment: Fragment): S {
        return getScreenOrNull(fragment)
            ?: throw IllegalArgumentException("Unknown error deserializing Screen. Check if args are null and Screen is Serializable or Parcelable.")
    }

}