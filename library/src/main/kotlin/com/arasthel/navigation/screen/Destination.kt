package com.arasthel.navigation.screen

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.arasthel.navigation.utils.ActivityConverter
import com.arasthel.navigation.utils.FragmentConverter
import kotlin.reflect.KClass

interface Destination {}

class ActivityDestination(
    val activityConverter: ActivityConverter<out Screen>,
    val activityClass: Class<out FragmentActivity>,
): Destination {

    fun createIntent(context: Context, screen: Screen, parentNavigatorId: String): Intent {
        return (activityConverter as ActivityConverter<Screen>).createIntent(context, screen)
    }

    fun configureIntent(intent: Intent, screen: Screen, parentNavigatorId: String) {
        (activityConverter as ActivityConverter<Screen>).configureIntent(intent, screen)
    }

}

class FragmentDestination(
    val fragmentConverter: FragmentConverter<out Screen>,
    val fragmentClass: Class<out Fragment>
): Destination {

    fun createFragment(screen: Screen, parentNavigatorId: String?): Fragment {
        return (fragmentConverter as FragmentConverter<Screen>).createFragment(screen, parentNavigatorId)
    }

    fun configureFragment(fragment: Fragment, screen: Screen, parentNavigatorId: String?) {
        (fragmentConverter as FragmentConverter<Screen>).configureFragment(fragment, screen, parentNavigatorId)
    }

}