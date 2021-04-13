package com.arasthel.navigation.navigators

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.arasthel.navigation.*
import com.arasthel.navigation.animations.AnimationData
import com.arasthel.navigation.base.NavigationFragment
import com.arasthel.navigation.base.getScreen
import com.arasthel.navigation.base.getScreenOrNull
import com.arasthel.navigation.screen.*

class FragmentNavigator(
    id: String,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    parent: Navigator?
): Navigator(id, parent) {

    val activityNavigator: ActivityNavigator? get() {
        var current = parent
        while (current != null && current !is ActivityNavigator) {
            current = (current as? FragmentNavigator)?.parent
        }
        return current as? ActivityNavigator
    }

    fun getFragments(): List<Fragment> {
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

    fun hasRootFragment(): Boolean = getFragments().isNotEmpty()

    inline fun <reified S: Screen> contains(screen: S): Boolean {
        val fragments = getFragments()
        return fragments.firstOrNull {
            val navigationFragment = it as? NavigationFragment ?: return@firstOrNull false
            navigationFragment.getScreenOrNull<S>() == screen
        } != null
    }

    fun contains(screenType: Class<Screen>): Boolean {
        val screenRegistry = AppNavigator.screenRegistry
        val destination = screenRegistry.getDestination(screenType)
        val destinationFragmentClass = (destination as? FragmentDestination)?.fragmentClass ?: return false

        val fragments = getFragments()
        return fragments.firstOrNull { destinationFragmentClass.isInstance(it) } != null
    }

    override fun push(navigationInstruction: NavigationInstruction) {
        push(navigationInstruction, null)
    }

    override fun push(navigationInstruction: NavigationInstruction, fromId: String?) {
        val screenRegistry = AppNavigator.screenRegistry
        val screen = navigationInstruction.screen
        val destination = screenRegistry.getDestination(screen)

        val fragments = getFragments()

        val currentFragment = fragments.lastOrNull() as? NavigationFragment
        val currentFragmentId = currentFragment?.let { it.fragmentId }
        val actualFromId = fromId ?: currentFragmentId

        when (destination) {
            is ActivityDestination -> activityNavigator?.push(navigationInstruction, actualFromId)
            is FragmentDestination -> {
                val fragment = destination.createFragment(screen, id)
                val count = fragments.count()
                val transaction = fragmentManager.beginTransaction()

                navigationInstruction.animationData?.toAnimation()?.setCustomAnimation(transaction)

                val transitionAnimation = navigationInstruction.animationData?.toOpenTransition()

                actualFromId?.let { fragment.arguments?.putString(PARENT_SCREEN_CLASS, it) }
                if (currentFragment != null) {
                    transitionAnimation?.applyBeforeFragmentTransaction(transaction, currentFragment, fragment)
                    fragment.arguments?.let { navigationInstruction.animationData?.saveAnimationData(it) }
                    transaction.detach(currentFragment)
                }

                transaction.add(containerId, fragment, getFragmentTag(count))
                        .commit()

                if (currentFragment != null && transitionAnimation != null) {
                    transitionAnimation.applyAfterFragmentTransaction(currentFragment, fragment)
                }
            }
        }
    }

    override fun pop() {
        val fragments = getFragments()
        val count = fragments.count()

        val currentFragment = fragments.lastOrNull() ?: return
        val previousFragment = if (count > 1) fragments[count - 2] else null

        val closingAnimation = AnimationData.getAnimationFrom(currentFragment.arguments)

        if (previousFragment != null) {
            val transaction = fragmentManager.beginTransaction()

            closingAnimation?.toCloseTransition()?.applyBeforeFragmentTransaction(transaction, currentFragment, previousFragment)
            transaction.remove(currentFragment)
                    .attach(previousFragment)
                    .commit()

            closingAnimation?.toCloseTransition()?.applyAfterFragmentTransaction(currentFragment, previousFragment)
        } else {
            parent?.pop()
        }
    }

    override fun <R : ScreenResult> popWithResult(result: R) {
        val fragments = getFragments()
        if (fragments.count() == 1) {
            parent?.popWithResult(result)
        } else {
            val fromScreenId = fragments.lastOrNull()?.arguments?.getString(PARENT_SCREEN_CLASS)
            if (fromScreenId != null) {
                publishResult(result, fromScreenId)
            }
            pop()
        }
    }

    override fun popUntil(screen: Screen): Boolean {
        return popUntilInternal { fragment ->
            val navigationFragment = fragment as? NavigationFragment ?: return@popUntilInternal false
            navigationFragment.getScreenOrNull(screen::class.java) == screen
        }
    }

    fun <S : Screen> popUntil(screenClass: Class<S>): Boolean {
        val screenRegistry = AppNavigator.screenRegistry
        val destination = screenRegistry.getDestination(screenClass) as? FragmentDestination ?: return false
        val fragmentClass = destination.fragmentClass

        return popUntilInternal { fragment ->
            fragmentClass.isInstance(fragment)
        }
    }

    private fun popUntilInternal(predicate: (Fragment) -> Boolean): Boolean {
        val fragments = getFragments()

        if (fragments.isEmpty()) return false

        val transaction = fragmentManager.beginTransaction()

        var destinationFragment: Fragment? = null
        var destinationIndex = -1

        for (i in (0 until fragments.count()).reversed()) {
            val fragment = fragments[i]
            if (predicate.invoke(fragment)) {
                destinationIndex = i
                destinationFragment = fragment
                break
            }
        }

        if (destinationFragment != null) {
            if (destinationIndex + 1 < fragments.size) {
                val sourceFragment = fragments[destinationIndex + 1]
                val closingAnimation = AnimationData.getAnimationFrom(sourceFragment.arguments)
                closingAnimation?.toCloseTransition()?.applyBeforeFragmentTransaction(transaction, sourceFragment, destinationFragment)
            }
            for (i in (destinationIndex+1 until fragments.count()).reversed()) {
                transaction.remove(fragments[i])
            }
            if (destinationFragment.isDetached) transaction.attach(destinationFragment)

            transaction.commit()

            return true
        } else {
            return false
        }
    }

    fun popToRoot(): Boolean {
        val fragments = getFragments()

        if (fragments.isEmpty()) return false

        val transaction = fragmentManager.beginTransaction()

        val destinationFragment = fragments.first()

        fragments.reversed().forEachIndexed { index, fragment ->
            when (index) {
                fragments.count() - 1 -> {
                    val sourceFragment = fragments[index]
                    val closingAnimation = AnimationData.getAnimationFrom(sourceFragment.arguments)
                    closingAnimation?.toCloseTransition()?.applyBeforeFragmentTransaction(transaction, sourceFragment, destinationFragment)
                    transaction.remove(sourceFragment)
                }
                0 -> transaction.attach(destinationFragment)
                else -> transaction.remove(fragments[index])
            }
        }

        transaction.commit()

        return true
    }

    fun removeFromBackstack(predicate: (Fragment) -> Boolean): Boolean {
        val fragments = getFragments()
        val foundFragment = fragments.firstOrNull { predicate.invoke(it) } ?: return false
        val transaction = fragmentManager.beginTransaction()
            .remove(foundFragment)

        if (fragments.count() > 0) {
            val tagField = Fragment::class.java.getDeclaredField("mTag")
            tagField.isAccessible = true

            val index = fragments.indexOf(foundFragment)
            for (i in (index until fragments.count())) {
                tagField.set(fragments[i], getFragmentTag(i - 1))
            }
        }

        transaction.commit()

        return true
    }

    override fun finish() {
        activityNavigator?.finish()
    }

    override fun replace(navigationInstruction: NavigationInstruction) {
        val screenRegistry = AppNavigator.screenRegistry
        val screen = navigationInstruction.screen
        val destination = screenRegistry.getDestination(screen)
        when (destination) {
            is ActivityDestination -> activityNavigator?.replace(navigationInstruction)
            is FragmentDestination -> {
                val destinationFragment = destination.createFragment(screen, id)
                val count = getFragments().count()
                val transaction = fragmentManager.beginTransaction()

                navigationInstruction.animationData?.toAnimation()?.setCustomAnimation(transaction)

                val transitionAnimation = navigationInstruction.animationData?.toOpenTransition()
                val currentFragment = getFragments().lastOrNull()
                if (currentFragment != null) {
                    transitionAnimation?.applyBeforeFragmentTransaction(transaction, currentFragment, destinationFragment)
                    destinationFragment.arguments?.let { navigationInstruction.animationData?.saveAnimationData(it) }
                    transaction.remove(currentFragment)
                }

                val index = if (count == 0) 0 else count-1
                transaction.add(containerId, destinationFragment, getFragmentTag(index))
                        .commit()

                transitionAnimation?.applyAfterFragmentTransaction(currentFragment, destinationFragment)
            }
        }
    }

    override fun reset(navigationInstruction: NavigationInstruction) {
        val fragments = getFragments()
        val count = fragments.size

        val screenRegistry = AppNavigator.screenRegistry
        val screen = navigationInstruction.screen
        val destination = screenRegistry.getDestination(screen)
        when (destination) {
            is ActivityDestination -> activityNavigator?.reset(navigationInstruction)
            is FragmentDestination -> {
                val destinationFragment = destination.createFragment(screen, id)

                val transaction = fragmentManager.beginTransaction()

                navigationInstruction.animationData?.toAnimation()?.setCustomAnimation(transaction)

                val transitionAnimation = navigationInstruction.animationData?.toOpenTransition()

                fragments.forEachIndexed { index, fragment ->
                    if (index == count - 1) {
                        transitionAnimation?.applyBeforeFragmentTransaction(transaction, fragment, destinationFragment)
                        destinationFragment.arguments?.let { navigationInstruction.animationData?.saveAnimationData(it) }
                    }
                    transaction.remove(fragment)
                }

                transaction.add(containerId, destinationFragment, getFragmentTag(0))
                        .commit()

                if (count > 0) {
                    transitionAnimation?.applyAfterFragmentTransaction(fragments.last(), destinationFragment)
                }
            }
        }
    }

    private fun getFragmentTag(index: Int): String {
        val indexValue = index.toString().padStart(3, '0')
        return "STACK_FRAGMENT_${containerId}_$indexValue"
    }

}