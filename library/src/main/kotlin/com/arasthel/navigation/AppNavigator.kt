package com.arasthel.navigation

import android.app.Application
import android.content.Context
import com.arasthel.navigation.navigators.ActivityNavigator
import com.arasthel.navigation.screen.ScreenRegistry
import com.arasthel.navigation.screen_result.ScreenResultHandler

object AppNavigator {

    lateinit var context: Context

    lateinit var activityNavigator: ActivityNavigator
        private set
    lateinit var screenRegistry: ScreenRegistry
        private set
    internal val screenResultHandler = ScreenResultHandler()

    fun initialize(application: Application, configuration: Configuration) {
        this.context = application

        this.activityNavigator = configuration.activityNavigator
            ?: throw IllegalArgumentException("Configuration must have an ActivityNavigator")
        this.screenRegistry = configuration.screenRegistry
            ?: throw IllegalArgumentException("Configuration must have an ScreenRegistry")

        application.registerActivityLifecycleCallbacks(activityNavigator)
    }

    class Configuration {
        var activityNavigator: ActivityNavigator? = null
        var screenRegistry: ScreenRegistry? = null
    }
}