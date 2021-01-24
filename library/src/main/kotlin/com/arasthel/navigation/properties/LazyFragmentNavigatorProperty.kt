package com.arasthel.navigation.properties

import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.arasthel.navigation.base.LifecycleAwareNavigationComponent
import com.arasthel.navigation.base.NavigationComponent
import com.arasthel.navigation.navigators.FragmentNavigator
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyFragmentNavigatorProperty(
    owner: LifecycleOwner,
    @IdRes val containerId: Int,
): ReadOnlyProperty<LifecycleOwner, FragmentNavigator> {

    private lateinit var childNavigator: FragmentNavigator

    init {
        owner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_CREATE) {
                    childNavigator = when (owner) {
                        is NavigationComponent -> owner.bindChildNavigator(owner.navigationId, containerId)
                        else -> throw IllegalArgumentException("Binding object is not a NavigationActivity or NavigationFramgent")
                    }
                }
            }
        })
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): FragmentNavigator {
        return childNavigator
    }
}

fun LifecycleAwareNavigationComponent.childNavigator(@IdRes containerId: Int) =
    LazyFragmentNavigatorProperty(this, containerId)