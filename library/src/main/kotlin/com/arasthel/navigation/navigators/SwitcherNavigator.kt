package com.arasthel.navigation.navigators

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.arasthel.navigation.*
import com.arasthel.navigation.base.NavigationFragment
import com.arasthel.navigation.screen.FragmentDestination
import com.arasthel.navigation.screen.Screen
import com.arasthel.navigation.screen.ScreenResult

class SwitcherNavigator(
    id: String,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    parent: Navigator?
): Navigator(id, parent) {

    init {
        initFragmentMap()
    }

    private var fragmentMap = mutableMapOf<Class<Screen>, Fragment>()

    override fun push(navigationInstruction: NavigationInstruction) {
        push(navigationInstruction, null)
    }

    override fun push(navigationInstruction: NavigationInstruction, fromId: String?) {
        parent?.push(navigationInstruction, (getCurrentFragment() as? NavigationFragment)?.fragmentId)
    }

    override fun pop() {
        parent?.pop()
    }

    override fun <R : ScreenResult> popWithResult(result: R) {
        parent?.popWithResult(result)
    }

    override fun popUntil(screen: Screen): Boolean {
        return parent?.popUntil(screen) ?: false
    }

    override fun finish() {
        parent?.finish()
    }

    override fun replace(navigationInstruction: NavigationInstruction) {
        parent?.replace(navigationInstruction)
    }

    override fun reset(navigationInstruction: NavigationInstruction) {
        parent?.reset(navigationInstruction)
    }

    fun switchTo(navigationInstruction: NavigationInstruction) {
        val currentFragment = getCurrentFragment()
        val screen = navigationInstruction.screen
        val destination = AppNavigator.screenRegistry.getDestination(screen)
        if (destination is FragmentDestination) {
            val destinationFragment = getOrCreateFragment(screen, destination)

            if (currentFragment == destinationFragment) {
                if (screen !== destination.fragmentConverter.getScreen(destinationFragment)) {
                    destination.updateScreen(destinationFragment, screen)
                    (destinationFragment as? NavigationFragment)?.onScreenUpdated()
                }
                return
            }

            val fragments = getFragments()
            val isNewFragment = !fragments.contains(destinationFragment)
            val transaction = fragmentManager.beginTransaction()

            navigationInstruction.animationData?.toAnimation()?.setCustomAnimation(transaction)

            val transitionAnimation = navigationInstruction.animationData?.toOpenTransition()
            if (currentFragment != null) {
                transitionAnimation?.applyBeforeFragmentTransaction(transaction, currentFragment, destinationFragment)
                transaction.detach(currentFragment)
            }
            if (isNewFragment) {
                transaction.add(containerId, destinationFragment, getFragmentTag(fragments.count()))
            } else {
                if (screen !== destination.fragmentConverter.getScreen(destinationFragment)) {
                    destination.updateScreen(destinationFragment, screen)
                    transaction.runOnCommit {
                        (destinationFragment as? NavigationFragment)?.onScreenUpdated()
                    }
                }
                transaction.attach(destinationFragment)
            }

            transaction.commit(navigationInstruction.fragmentOptions)

            if (currentFragment != null) {
                transitionAnimation?.applyAfterFragmentTransaction(currentFragment, destinationFragment)
            }
        }
    }

    private fun initFragmentMap() {
        fragmentMap = getFragments().mapNotNull { fragment ->
            val screen = getScreen(fragment) ?: return@mapNotNull null
            screen.javaClass to fragment
        }.toMap().toMutableMap()
    }

    private fun getFragments(): List<Fragment> {
        val result = mutableListOf<Fragment>()
        var index = 0
        while (true) {
            val tag = getFragmentTag(index)
            val fragment = fragmentManager.findFragmentByTag(tag) ?: break
            if (!fragment.isRemoving) {
                result.add(fragment)
            }
            index++
        }
        return result
    }

    private fun <S: Screen> getOrCreateFragment(screen: S, destination: FragmentDestination): Fragment {
        var fragment = fragmentMap[screen.javaClass]
        if (fragment == null) {
            fragment = destination.createFragment(screen, id)
            fragmentMap[screen.javaClass] = fragment
            return fragment
        }
        return fragment
    }

    private fun getCurrentScreen(): Screen? {
        return getCurrentFragment()?.let { getScreen(it) }
    }

    private fun getScreen(fragment: Fragment): Screen? {
        val destination = AppNavigator.screenRegistry.getDestination(fragment)
        return (destination as? FragmentDestination)?.fragmentConverter?.getScreen(fragment)
    }

    private fun getCurrentFragment(): Fragment? {
        return fragmentManager.findFragmentById(containerId)
    }

    private fun getFragmentTag(index: Int): String {
        return "SWITCHER_FRAGMENT_${containerId}_$index"
    }

}