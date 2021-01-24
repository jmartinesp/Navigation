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
    val extras: Bundle?
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