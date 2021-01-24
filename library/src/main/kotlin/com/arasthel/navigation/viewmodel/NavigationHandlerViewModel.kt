package com.arasthel.navigation.viewmodel

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.fragment.app.viewModels
import com.arasthel.navigation.base.NavigationActivity
import com.arasthel.navigation.navigators.Navigator

class NavigationHandlerViewModel: ViewModel() {

    private val navigationContexts = mutableMapOf<String, Navigator>()

    fun onContextBound(navigator: Navigator) {
        navigationContexts[navigator.id] = navigator
    }

    fun getNavigationContext(id: String): Navigator? {
        return navigationContexts[id]
    }

}

class NavigationHandlerViewModelFactory(

): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NavigationHandlerViewModel() as T
    }
}

internal fun ViewModelStoreOwner.createNavigationHandleViewModel(

): NavigationHandlerViewModel {
    return when(this) {
        is Fragment -> viewModels<NavigationHandlerViewModel>(ownerProducer = { requireActivity() }) {
            NavigationHandlerViewModelFactory()
        }.value
        is FragmentActivity -> viewModels<NavigationHandlerViewModel> {
            NavigationHandlerViewModelFactory()
        }.value
        else -> throw IllegalArgumentException("ViewModelStoreOwner must be a Fragment or Activity")
    }
}

internal fun ViewModelStoreOwner.getNavigationHandleViewModel(): NavigationHandlerViewModel {
    return when(this) {
        is FragmentActivity -> viewModels<NavigationHandlerViewModel> { ViewModelProvider.NewInstanceFactory() }.value
        is Fragment -> viewModels<NavigationHandlerViewModel>(ownerProducer = { requireActivity() }) { ViewModelProvider.NewInstanceFactory() }.value
        else -> throw IllegalArgumentException("ViewModelStoreOwner must be a Fragment or Activity")
    }
}

/// FragmentActivity

fun NavigationActivity.bindContext(navigator: Navigator) {
    val viewModel = createNavigationHandleViewModel()
    viewModel.onContextBound(navigator)
}

fun NavigationActivity.getNavigationContext(id: String): Navigator? {
    val viewModel = getNavigationHandleViewModel()
    return viewModel.getNavigationContext(id)
}

/// Fragment

fun Fragment.bindContext(navigator: Navigator) {
    val viewModel = createNavigationHandleViewModel()
    viewModel.onContextBound(navigator)
}

fun Fragment.getNavigationContext(id: String): Navigator? {
    val viewModel = getNavigationHandleViewModel()
    return viewModel.getNavigationContext(id)
}
