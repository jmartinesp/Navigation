package com.arasthel.navigation_experiment

import android.os.Bundle
import android.os.Parcelable
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import com.arasthel.navigation.annotations.RegisterScreen
import com.arasthel.navigation.base.NavigationActivity
import com.arasthel.navigation.base.getScreen
import com.arasthel.navigation.navigators.fade
import com.arasthel.navigation.navigators.getScreen
import com.arasthel.navigation.navigators.getScreenOrNull
import com.arasthel.navigation.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize data class OtherScreen(val test: String): Screen, Parcelable

@RegisterScreen(OtherScreen::class)
class OtherActivity: NavigationActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println(getScreenOrNull<OtherScreen>(intent)?.test)
    }

    override fun onBackPressed() {
        getActivityNavigator().popWithResult(TestResult(1234))
    }

}