package com.arasthel.navigation.properties

import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.arasthel.navigation.base.LifecycleAwareNavigationComponent
import com.arasthel.navigation.base.NavigationComponent
import com.arasthel.navigation.navigators.FragmentNavigator
import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyFragmentNavigatorProperty(
    owner: LifecycleOwner,
    @IdRes val containerId: Int,
): ReadOnlyProperty<LifecycleOwner, FragmentNavigator> {

    private var childNavigator: FragmentNavigator? = null
    private val ownerRef = WeakReference(owner)

    init {
        owner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                val owner = ownerRef.get() ?: return
                if (event == Lifecycle.Event.ON_CREATE) {
                    childNavigator = when (owner) {
                        is NavigationComponent -> {
                            owner.bindChildNavigator(owner.navigationId, containerId)
                        }
                        else -> throw IllegalArgumentException("Binding object is not a NavigationActivity or NavigationFragment")
                    }
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    childNavigator = null
                }
            }
        })
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): FragmentNavigator {
        return childNavigator ?: throw IllegalStateException("Accessed childNavigator when parent component was destroyed")
    }
}

fun LifecycleAwareNavigationComponent.childNavigator(@IdRes containerId: Int) =
    LazyFragmentNavigatorProperty(this, containerId)