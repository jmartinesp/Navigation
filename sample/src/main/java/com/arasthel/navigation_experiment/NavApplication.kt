package com.arasthel.navigation_experiment

import android.app.Application
import com.arasthel.navigation.AppNavigator
import com.arasthel.navigation.navigators.MainActivityNavigator
import com.arasthel.navigation.screen.GeneratedScreenRegistry

class NavApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        val configuration = AppNavigator.Configuration().apply {
            screenRegistry = GeneratedScreenRegistry()
            activityNavigator = MainActivityNavigator()
        }

        AppNavigator.initialize(this, configuration)
    }

}