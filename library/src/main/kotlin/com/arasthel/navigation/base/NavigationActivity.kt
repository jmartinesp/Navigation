package com.arasthel.navigation.base

import android.os.Bundle
import android.view.Window
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.arasthel.navigation.AppNavigator
import com.arasthel.navigation.animations.AnimationData
import com.arasthel.navigation.navigators.FragmentNavigator
import com.arasthel.navigation.navigators.Navigator
import com.arasthel.navigation.navigators.SwitcherNavigator
import com.arasthel.navigation.viewmodel.bindContext
import com.arasthel.navigation.viewmodel.getNavigationContext
import java.util.UUID

open class NavigationActivity: AppCompatActivity, LifecycleAwareNavigationComponent {

    companion object {
        const val FRAGMENT_NAV_ID = "HOST_ACTIVITY_FRAGMENT_STACK"
        const val SWITCHER_ID = "HOST_ACTIVITY_SWITCHER"

        const val ACTIVITY_ID = "ACTIVITY_ID"
    }

    lateinit var id: String
    override val navigationId: String by lazy { id }

    constructor(): super()
    constructor(@LayoutRes layoutRes: Int): super(layoutRes)

    override fun onCreate(savedInstanceState: Bundle?) {
        val transitionAnimation = AnimationData.getAnimationFrom(intent.extras)
        if (transitionAnimation != null) {
            window?.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            window?.allowEnterTransitionOverlap = true
            window?.allowReturnTransitionOverlap = true
            transitionAnimation.toOpenTransition()?.applyAfterActivityStarted(this)
            transitionAnimation.toCloseTransition()?.applyAfterActivityFinished(this)
        }

        super.onCreate(savedInstanceState)

        restoreNavigationId(savedInstanceState)
    }

    fun bindChildNavigator(@IdRes containerId: Int): FragmentNavigator {
        return bindChildNavigator(FRAGMENT_NAV_ID, containerId)
    }

    override fun bindChildNavigator(id: String, @IdRes containerId: Int): FragmentNavigator {
        val navigationContext = FragmentNavigator(
            id = id,
            fragmentManager = supportFragmentManager,
            containerId = containerId,
            parent = AppNavigator.activityNavigator
        )
        bindContext(navigationContext)
        return navigationContext
    }

    fun bindSwitcherNavigator(@IdRes containerId: Int): SwitcherNavigator {
        return bindSwitcherNavigator(SWITCHER_ID, containerId)
    }

    override fun bindSwitcherNavigator(id: String, @IdRes containerId: Int): SwitcherNavigator {
        val switcherNavigationContext = SwitcherNavigator(
                id = id,
                fragmentManager = supportFragmentManager,
                containerId = containerId,
                parent = AppNavigator.activityNavigator
        )
        bindContext(switcherNavigationContext)
        return switcherNavigationContext
    }

    override fun getChildNavigator(): Navigator? {
        return getNavigationContext(FRAGMENT_NAV_ID)
    }

    fun getActivityNavigator(): Navigator {
        return AppNavigator.activityNavigator
    }

    override fun getParentNavigator(): Navigator? {
        return getActivityNavigator()
    }

    override fun getSwitcherNavigator(): SwitcherNavigator? {
        return getNavigationContext(SWITCHER_ID) as? SwitcherNavigator
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        saveNavigationId(outState)
    }

    override fun saveNavigationId(savedInstanceState: Bundle) {
        savedInstanceState.putString(ACTIVITY_ID, navigationId)
    }

    override fun restoreNavigationId(savedInstanceState: Bundle?) {
        id = savedInstanceState?.getString(ACTIVITY_ID) ?: "${this::class.java.name}:${UUID.randomUUID()}"
    }

    override fun goBack() {
        finish()
    }

}