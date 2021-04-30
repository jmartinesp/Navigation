package com.arasthel.navigation.properties

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.arasthel.navigation.base.LifecycleAwareNavigationComponent
import com.arasthel.navigation.base.NavigationComponent
import com.arasthel.navigation.navigators.Navigator
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyParentNavigatorProperty(
    owner: LifecycleOwner,
): ReadOnlyProperty<LifecycleOwner, Navigator?> {

    private var parentNavigator: Navigator? = null

    init {
        owner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_START) {
                    parentNavigator = when (owner) {
                        is NavigationComponent -> owner.getParentNavigator()
                        else -> throw IllegalArgumentException("Binding object is not a NavigationActivity or NavigationFragment")
                    }
                } else if (event == Lifecycle.Event.ON_STOP) {
                    parentNavigator = null
                }
            }
        })
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): Navigator? {
        return parentNavigator
    }
}

fun LifecycleAwareNavigationComponent.parentNavigator() = LazyParentNavigatorProperty(this)