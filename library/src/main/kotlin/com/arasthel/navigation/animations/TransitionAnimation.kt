package com.arasthel.navigation.animations

import android.app.Activity
import android.transition.Transition
import androidx.annotation.TransitionRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction

interface TransitionAnimation {

    fun applyBeforeFragmentTransaction(
            transaction: FragmentTransaction,
            sourceFragment: Fragment?,
            destinationFragment: Fragment
    )

    fun applyAfterFragmentTransaction(
            sourceFragment: Fragment?,
            destinationFragment: Fragment
    )

    fun applyAfterActivityStarted(activity: Activity)
    fun applyAfterActivityFinished(activity: Activity)

}

class ActivityTransitionAnimation(
    @TransitionRes private val enter: Int,
    @TransitionRes private val exit: Int
): TransitionAnimation {

    override fun applyBeforeFragmentTransaction(
        transaction: FragmentTransaction,
        sourceFragment: Fragment?,
        destinationFragment: Fragment
    ) {

    }

    override fun applyAfterFragmentTransaction(
        sourceFragment: Fragment?,
        destinationFragment: Fragment
    ) {

    }

    override fun applyAfterActivityStarted(activity: Activity) {
        activity.overridePendingTransition(enter, exit)
    }

    override fun applyAfterActivityFinished(activity: Activity) {
        activity.overridePendingTransition(enter, exit)
    }

}

open class SimpleTransitionAnimation(
        private val enter: Transition,
        private val exit: Transition
): TransitionAnimation {
    override fun applyBeforeFragmentTransaction(transaction: FragmentTransaction, sourceFragment: Fragment?, destinationFragment: Fragment) {
        sourceFragment?.exitTransition = exit
        destinationFragment.enterTransition = enter
        destinationFragment.allowEnterTransitionOverlap = true
    }

    override fun applyAfterFragmentTransaction(sourceFragment: Fragment?, destinationFragment: Fragment) {
        sourceFragment?.exitTransition = null
        destinationFragment.enterTransition = null
        destinationFragment.allowEnterTransitionOverlap = true
    }

    override fun applyAfterActivityStarted(activity: Activity) {
        activity.window?.enterTransition = enter
    }
    override fun applyAfterActivityFinished(activity: Activity) {
        activity.window?.returnTransition = exit
    }
}