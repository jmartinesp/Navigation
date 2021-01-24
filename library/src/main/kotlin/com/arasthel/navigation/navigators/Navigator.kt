package com.arasthel.navigation.navigators

import com.arasthel.navigation.AppNavigator
import com.arasthel.navigation.screen_result.ResultChannelId
import com.arasthel.navigation.screen.Screen
import com.arasthel.navigation.screen.ScreenResult

abstract class Navigator(
    val id: String,
    val parent: Navigator?
) {

    companion object {
        const val PARENT_SCREEN_CLASS = "PARENT_SCREEN_CLASS"
        const val PARENT_NAVIGATOR_ID = "PARENT_NAVIGATOR_ID"
    }

    abstract fun push(navigationInstruction: NavigationInstruction)
    internal abstract fun push(navigationInstruction: NavigationInstruction, fromId: String?)
    abstract fun pop()
    abstract fun <R: ScreenResult> popWithResult(result: R)
    abstract fun popUntil(screen: Screen): Boolean
    abstract fun replace(navigationInstruction: NavigationInstruction)
    abstract fun reset(navigationInstruction: NavigationInstruction)
    abstract fun finish()

    fun <T: ScreenResult> registerResultListener(id: String, resultClass: Class<T>, listener: (T) -> Unit): ResultChannelId {
        return AppNavigator.screenResultHandler.registerResultListener(id, resultClass, listener)
    }

    fun unregisterResultListener(id: ResultChannelId) {
        return AppNavigator.screenResultHandler.unregisterResultListener(id)
    }

    fun <T: ScreenResult> publishResult(result: T, screenId: String) {
        AppNavigator.screenResultHandler.publishResultToChannels(result, screenId)
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Navigator -> id == other.id
            else -> false
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}