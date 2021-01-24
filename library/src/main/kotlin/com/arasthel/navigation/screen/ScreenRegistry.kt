package com.arasthel.navigation.screen

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.arasthel.navigation.utils.ActivityConverter
import com.arasthel.navigation.utils.FragmentConverter
import kotlin.reflect.KClass

open class ScreenRegistry {

    private val destinations = mutableMapOf<Class<*>, Destination>()

    fun <S: Screen, A: FragmentActivity, R: ScreenResult> registerActivityForResult(
        screen: Class<S>,
        activityClass: Class<A>,
        resultClass: Class<R>
    ) {
        registerActivity(screen, activityClass)
    }

    fun <S: Screen, A: FragmentActivity> registerActivity(screen: Class<S>, activityClass: Class<A>) {
        val activityConverter = ActivityConverter(screen, activityClass)
        val destination = ActivityDestination(activityConverter, activityClass)
        destinations[screen] = destination
    }

    fun <S: Screen, F: Fragment, R: ScreenResult> registerFragmentForResult(
        screen: Class<S>,
        fragmentClass: Class<F>,
        resultClass: Class<R>
    ) {
        registerFragment(screen, fragmentClass)
    }

    fun <S: Screen, F: Fragment> registerFragment(screen: Class<S>, fragmentClass: Class<F>) {
        val converter = FragmentConverter(screen, fragmentClass)
        val destination = FragmentDestination(converter, fragmentClass)
        destinations[screen] = destination
    }

    fun <S: Screen> getDestination(screen: S): Destination? {
        return getDestination(screen::class.java)
    }

    fun <S: Screen> getDestination(screenClass: Class<S>): Destination? {
        return destinations[screenClass]
    }

    fun getScreenClass(fragment: Fragment): Class<out Screen>? {
        return fragment.arguments?.getString(FragmentConverter.SCREEN_CLASS)?.let { Class.forName(it) } as? Class<Screen>
    }

    fun getDestination(fragment: Fragment): Destination? {
        return getScreenClass(fragment)?.let { destinations[it] }
    }

}