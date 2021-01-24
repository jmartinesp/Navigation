package com.arasthel.navigation.properties

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.arasthel.navigation.base.LifecycleAwareNavigationComponent
import com.arasthel.navigation.base.NavigationFragment
import com.arasthel.navigation.navigators.Navigator
import com.arasthel.navigation.screen.ScreenResult
import com.arasthel.navigation.screen_result.ResultChannelId
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified R: ScreenResult> NavigationFragment.registerForResult(
    noinline listener: (R) -> Unit,
): FragmentScreenResultObserverProperty<R> {
    return FragmentScreenResultObserverProperty(this, R::class.java, listener)
}

inline fun <reified R: ScreenResult> LifecycleAwareNavigationComponent.registerForResult(
    noinline listener: (R) -> Unit,
): ScreenResultObserverProperty<LifecycleAwareNavigationComponent, R> {
    return ScreenResultObserverProperty(this, R::class.java, listener)
}

class ScreenResultObserverProperty<O: LifecycleAwareNavigationComponent, R: ScreenResult>(
    owner: O,
    val resultClass: Class<R>,
    val listener: (R) -> Unit,
): ReadOnlyProperty<O, ScreenResultObserver<R>> {

    private var resultObserver: ScreenResultObserver<R>? = null

    init {
        val lifecycleOwner = owner as LifecycleOwner
        lifecycleOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event != Lifecycle.Event.ON_START) return

                val navigationContext = owner.getParentNavigator() ?: return
                val id = owner.navigationId

                resultObserver =
                    ScreenResultObserver(navigationContext, id, resultClass, listener)
                        .also { it.register() }
            }
        })

        lifecycleOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    resultObserver?.unregister()
                    resultObserver = null
                }
            }
        })

    }

    /// Used for Fragment's viewLifecycleOwner
    private fun registerCustomOwner(actualOwner: O, customOwner: LifecycleOwner) {
        customOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event != Lifecycle.Event.ON_START) return

                val navigationContext = actualOwner.getParentNavigator() ?: return
                val id = actualOwner.navigationId

                resultObserver = ScreenResultObserver(navigationContext, id, resultClass, listener)
                    .also { it.register() }
            }
        })

        customOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    resultObserver?.unregister()
                    resultObserver = null
                }
            }
        })
    }

    override fun getValue(
        thisRef: O,
        property: KProperty<*>
    ): ScreenResultObserver<R> = resultObserver!!

}

class FragmentScreenResultObserverProperty<R: ScreenResult>(
    owner: NavigationFragment,
    val resultClass: Class<R>,
    val listener: (R) -> Unit,
): ReadOnlyProperty<NavigationFragment, ScreenResultObserver<R>> {

    private var resultObserver: ScreenResultObserver<R>? = null

    init {
        val lifecycleOwner = owner as LifecycleOwner
        lifecycleOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event != Lifecycle.Event.ON_START) return

                registerViewLifecycleOwner(owner)
            }
        })
    }

    /// Used for Fragment's viewLifecycleOwner
    private fun registerViewLifecycleOwner(fragmentOwner: NavigationFragment) {
        fragmentOwner.viewLifecycleOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event != Lifecycle.Event.ON_START) return

                val navigationContext = fragmentOwner.getParentNavigator() ?: return
                val id = fragmentOwner.navigationId

                resultObserver = ScreenResultObserver(navigationContext, id, resultClass, listener)
                    .also { it.register() }
            }
        })

        fragmentOwner.viewLifecycleOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    resultObserver?.unregister()
                    resultObserver = null
                }
            }
        })
    }

    override fun getValue(
        thisRef: NavigationFragment,
        property: KProperty<*>
    ): ScreenResultObserver<R> = resultObserver!!

}

class ScreenResultObserver<R: ScreenResult>(
    private val navigator: Navigator,
    val id: String,
    val resultClass: Class<R>,
    val listener: (R) -> Unit,
) {

    private var resultChannelId: ResultChannelId? = null

    fun register() {
        resultChannelId = navigator.registerResultListener(id, resultClass, listener)
    }

    fun unregister() {
        resultChannelId?.let { navigator.unregisterResultListener(it) }
        resultChannelId = null
    }
}