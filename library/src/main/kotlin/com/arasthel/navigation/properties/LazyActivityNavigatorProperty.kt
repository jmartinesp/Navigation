package com.arasthel.navigation.properties

import com.arasthel.navigation.navigators.ActivityNavigator
import com.arasthel.navigation.AppNavigator
import com.arasthel.navigation.base.NavigationActivity
import com.arasthel.navigation.base.NavigationComponent
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyActivityNavigatorProperty: ReadOnlyProperty<NavigationActivity, ActivityNavigator> {

    override fun getValue(thisRef: NavigationActivity, property: KProperty<*>): ActivityNavigator {
        return AppNavigator.activityNavigator
    }

}

fun NavigationComponent.activityNavigator() = LazyActivityNavigatorProperty()