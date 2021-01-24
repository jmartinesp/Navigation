package com.arasthel.navigation.base

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.lifecycle.LifecycleOwner
import com.arasthel.navigation.navigators.FragmentNavigator
import com.arasthel.navigation.navigators.Navigator
import com.arasthel.navigation.navigators.SwitcherNavigator

interface NavigationComponent {
    val navigationId: String

    fun bindChildNavigator(id: String, @IdRes containerId: Int): FragmentNavigator
    fun bindSwitcherNavigator(id: String, @IdRes containerId: Int): SwitcherNavigator

    fun getParentNavigator(): Navigator?
    fun getSwitcherNavigator(): Navigator?
    fun getChildNavigator(): Navigator?

    fun saveNavigationId(savedInstanceState: Bundle)
    fun restoreNavigationId(savedInstanceState: Bundle?)

    fun goBack()

}

interface LifecycleAwareNavigationComponent: LifecycleOwner, NavigationComponent