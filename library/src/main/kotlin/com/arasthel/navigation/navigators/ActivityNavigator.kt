package com.arasthel.navigation.navigators

import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import com.arasthel.navigation.AppNavigator
import com.arasthel.navigation.base.NavigationComponent
import com.arasthel.navigation.screen.ActivityDestination
import com.arasthel.navigation.screen.Screen
import com.arasthel.navigation.screen.ScreenResult

class ActivityNavigator(
    id: String,
    currentActivity: FragmentActivity?,
): Navigator(id, null) {

    private val screenRegistry by lazy { AppNavigator.screenRegistry }

    var currentActivity: FragmentActivity? = currentActivity
        private set

    fun updateCurrentActivity(activity: FragmentActivity) {
        currentActivity = activity
    }

    override fun push(navigationInstruction: NavigationInstruction) {
        push(navigationInstruction, null)
    }

    override fun push(navigationInstruction: NavigationInstruction, fromId: String?) {
        val currentActivity = this.currentActivity ?: return
        val screen = navigationInstruction.screen
        val destination = screenRegistry.getDestination(screen) as? ActivityDestination ?: return
        val intent = destination.createIntent(currentActivity, screen, id)
        val activityId = (this.currentActivity as? NavigationComponent)?.navigationId
        (fromId ?: activityId)?.let { intent.putExtra(PARENT_SCREEN_CLASS, it) }

        val animationData = navigationInstruction.animationData
        intent.extras?.let { extras ->
            animationData?.saveAnimationData(extras)
            intent.replaceExtras(extras)
        }

        val options = if (animationData != null)
            ActivityOptionsCompat.makeSceneTransitionAnimation(currentActivity).toBundle()
            else null
        ActivityCompat.startActivity(currentActivity, intent, options)
        animationData?.toActivityAnimation()?.applyAfterActivityStarted(currentActivity)
    }

    override fun pop() {
        currentActivity?.finishAfterTransition()
    }

    override fun <R : ScreenResult> popWithResult(result: R) {
        currentActivity?.finishAfterTransition()
        val fromScreenId = currentActivity?.intent?.getStringExtra(PARENT_SCREEN_CLASS)
        if (fromScreenId != null) {
            publishResult(result, fromScreenId)
        }
    }

    override fun popUntil(screen: Screen): Boolean {
        val context = currentActivity ?: return false
        val destination = screenRegistry.getDestination(screen) as? ActivityDestination ?: return false
        val intent = destination.createIntent(context, screen, id)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        ActivityCompat.startActivity(context, intent, null)
        return true
    }

    override fun finish() {
        currentActivity?.finishAfterTransition()
    }

    override fun replace(navigationInstruction: NavigationInstruction) {
        val currentActivity = currentActivity ?: return
        val screen = navigationInstruction.screen

        val destination = screenRegistry.getDestination(screen) as? ActivityDestination ?: return
        val intent = destination.createIntent(currentActivity, screen, id)

        val animationData = navigationInstruction.animationData
        intent.extras?.let { extras ->
            animationData?.saveAnimationData(extras)
            intent.replaceExtras(extras)
        }

        val options = if (animationData != null)
            ActivityOptionsCompat.makeSceneTransitionAnimation(currentActivity).toBundle()
            else null
        ActivityCompat.startActivity(currentActivity, intent, options)

        navigationInstruction.animationData?.toActivityAnimation()?.applyAfterActivityStarted(currentActivity)

        currentActivity.finishAfterTransition()
        navigationInstruction.animationData?.toActivityAnimation()?.applyAfterActivityFinished(currentActivity)
    }

    override fun reset(navigationInstruction: NavigationInstruction) {
        val currentActivity = this.currentActivity ?: return
        val screen = navigationInstruction.screen
        val destination = screenRegistry.getDestination(screen) as? ActivityDestination ?: return
        val intent = destination.createIntent(currentActivity, screen, id)
        if (navigationInstruction.activityOptions?.overrideFlags == true) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val animationData = navigationInstruction.animationData
        intent.extras?.let { extras ->
            animationData?.saveAnimationData(extras)
            intent.replaceExtras(extras)
        }

        val options = if (animationData != null)
            ActivityOptionsCompat.makeSceneTransitionAnimation(currentActivity).toBundle()
            else null
        ActivityCompat.startActivity(currentActivity, intent, options)
        animationData?.toActivityAnimation()?.applyAfterActivityStarted(currentActivity)
    }

}

fun MainActivityNavigator(): ActivityNavigator {
    return ActivityNavigator("APP_ACTIVITY_NAVIGATOR", null)
}