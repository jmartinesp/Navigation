package com.arasthel.navigation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.arasthel.navigation.navigators.FragmentNavigator
import com.arasthel.navigation.navigators.Navigator
import com.arasthel.navigation.navigators.SwitcherNavigator
import com.arasthel.navigation.screen.Screen
import com.arasthel.navigation.utils.ActivityConverter
import com.arasthel.navigation.utils.FragmentConverter
import com.arasthel.navigation.viewmodel.bindContext
import com.arasthel.navigation.viewmodel.getNavigationContext
import java.util.*

open class NavigationFragment(): Fragment(), LifecycleAwareNavigationComponent {

    companion object {
        const val FRAGMENT_ID = "FRAGMENT_ID"
    }

    private var layoutRes: Int? = null

    lateinit var fragmentId: String
    override val navigationId: String by lazy { fragmentId }

    constructor(@LayoutRes layoutRes: Int): this() {
        this.layoutRes = layoutRes
    }

    constructor(@LayoutRes layoutRes: Int, args: Bundle?): this(layoutRes) {
        args?.let { this.arguments = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreNavigationId(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutRes?.let { inflater.inflate(it, container, false) }
    }

    private val onBackPressedCallback = object: OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            goBack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context as AppCompatActivity).onBackPressedDispatcher.addCallback(onBackPressedCallback)
        onBackPressedCallback.isEnabled = true
    }

    override fun onDetach() {
        super.onDetach()

        onBackPressedCallback.isEnabled = false
    }

    override fun bindChildNavigator(id: String, @IdRes containerId: Int): FragmentNavigator {
        val parent = (activity as? NavigationActivity)?.getChildNavigator() ?:
                (parentFragment as? NavigationFragment)?.getChildNavigator()
        val navigationContext = FragmentNavigator(
            id = id,
            fragmentManager = childFragmentManager,
            containerId = containerId,
            parent = parent
        )
        bindContext(navigationContext)
        return navigationContext
    }

    override fun bindSwitcherNavigator(id: String, containerId: Int): SwitcherNavigator {
        val parent = (activity as? NavigationActivity)?.getChildNavigator() ?:
        (parentFragment as? NavigationFragment)?.getChildNavigator()
        val navigationContext = SwitcherNavigator(
            id = "SWITCHER_$id",
            fragmentManager = childFragmentManager,
            containerId = containerId,
            parent = parent
        )
        bindContext(navigationContext)
        return navigationContext
    }

    override fun getParentNavigator(): Navigator? {
        val id = arguments?.getString(Navigator.PARENT_NAVIGATOR_ID) ?: return null
        return getNavigationContext(id)
    }

    override fun getChildNavigator(): Navigator? {
        return getNavigationContext(navigationId)
    }

    override fun getSwitcherNavigator(): Navigator? {
        return getNavigationContext("SWITCHER_$navigationId")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        saveNavigationId(outState)
    }

    override fun goBack() {
        getParentNavigator()?.pop()
    }

    override fun saveNavigationId(savedInstanceState: Bundle) {
        savedInstanceState.putString(FRAGMENT_ID, navigationId)
    }

    override fun restoreNavigationId(savedInstanceState: Bundle?) {
        fragmentId = savedInstanceState?.getString(FRAGMENT_ID)
            ?: "${this::class.java.name}:${UUID.randomUUID()}"
    }

}

fun <S: Screen> NavigationFragment.getScreenOrNull(clazz: Class<S>): S? {
    val fragmentConverter = FragmentConverter(clazz, this::class.java)
    return fragmentConverter.getScreenOrNull(this)
}

fun <S: Screen> NavigationFragment.getScreen(clazz: Class<S>): S? {
    val fragmentConverter = FragmentConverter(clazz, this::class.java)
    return fragmentConverter.getScreenOrNull(this)
}

inline fun <reified S: Screen> NavigationFragment.getScreenOrNull(): S? {
    val fragmentConverter = FragmentConverter(S::class.java, this::class.java)
    return fragmentConverter.getScreenOrNull(this)
}

inline fun <reified S: Screen> NavigationFragment.getScreen(): S {
    val fragmentConverter = FragmentConverter(S::class.java, this::class.java)
    return fragmentConverter.getScreen(this)
}

inline fun <reified S: Screen> NavigationActivity.getScreen(): S {
    val activityConverter = ActivityConverter(S::class.java, this::class.java)
    return activityConverter.getScreen(this)
}