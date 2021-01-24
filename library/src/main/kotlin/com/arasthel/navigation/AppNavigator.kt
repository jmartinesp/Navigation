package com.arasthel.navigation

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.arasthel.navigation.base.NavigationActivity
import com.arasthel.navigation.base.NavigationComponent
import com.arasthel.navigation.navigators.ActivityNavigator
import com.arasthel.navigation.screen.ScreenRegistry
import com.arasthel.navigation.screen_result.ScreenResultHandler
import com.arasthel.navigation.viewmodel.bindContext
import java.lang.IllegalArgumentException

object AppNavigator {

    lateinit var context: Context

    lateinit var activityNavigator: ActivityNavigator
        private set
    lateinit var screenRegistry: ScreenRegistry
        private set
    internal val screenResultHandler = ScreenResultHandler()

    fun initialize(context: Context, configuration: Configuration) {
        this.context = context

        this.activityNavigator = configuration.activityNavigator
            ?: throw IllegalArgumentException("Configuration must have an ActivityNavigator")
        this.screenRegistry = configuration.screenRegistry
            ?: throw IllegalArgumentException("Configuration must have an ScreenRegistry")
    }

    fun bindCurrentActivityToNavigationContext(activity: FragmentActivity) {
        (activity as? NavigationActivity)?.bindContext(activityNavigator)
        activityNavigator.updateCurrentActivity(activity)
    }

    class Configuration {
        var activityNavigator: ActivityNavigator? = null
        var screenRegistry: ScreenRegistry? = null
    }
}