package com.arasthel.navigation.navigators

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionSet
import android.view.Gravity
import com.arasthel.navigation.animations.AnimationData
import com.arasthel.navigation.screen.Screen

data class NavigationInstruction(
    val screen: Screen,
    val animationData: AnimationData?,
    val extras: Bundle?,
    var activityOptions: ActivityOptions? = null,
    var fragmentOptions: FragmentOptions? = null,
)

fun Screen.noAnimation(): NavigationInstruction {
    return NavigationInstruction(this, null, null)
}

fun Screen.fade(): NavigationInstruction {
    val animationData = AnimationData.Fade
    return NavigationInstruction(this, animationData, null)
}

fun Screen.horizontal(): NavigationInstruction {
    val animationData = AnimationData.Slide
    return NavigationInstruction(this, animationData, null)
}

fun Screen.vertical(): NavigationInstruction {
    val animationData = AnimationData.Modal
    return NavigationInstruction(this, animationData, null)
}

fun Screen.default(): NavigationInstruction {
    val animationData = AnimationData.Default
    return NavigationInstruction(this, animationData, null)
}

fun NavigationInstruction.activityOptions(builder: ActivityOptions.() -> Unit): NavigationInstruction {
    this.activityOptions = ActivityOptions().also(builder)
    return this
}

fun NavigationInstruction.fragmentOptions(builder: FragmentOptions.() -> Unit): NavigationInstruction {
    this.fragmentOptions = FragmentOptions().also(builder)
    return this
}

class ActivityOptions {
    var overrideFlags: Boolean = true
}

class FragmentOptions {
    var allowStateLoss: Boolean = false
    var immediate: Boolean = false
}