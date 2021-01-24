package com.arasthel.navigation.animations

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.transition.*
import android.view.Gravity
import androidx.annotation.AnimRes
import androidx.annotation.TransitionRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.arasthel.navigation.AppNavigator
import kotlinx.android.parcel.Parcelize

sealed class AnimationData {

    companion object {
        private const val TRANSITION_ANIMATION_TYPE = "navigator_transition_animation_type"
        private const val TRANSITION_ANIMATION = "navigator_transition_animation_type"

        fun getAnimationFrom(bundle: Bundle?): AnimationData? {
            val transitionType = bundle?.getString(TRANSITION_ANIMATION_TYPE)
            return when (transitionType) {
                Slide.KEY -> Slide
                Modal.KEY -> Modal
                Fade.KEY -> Fade
                Default.KEY -> Default
                Resource.Animation.KEY -> bundle.getParcelable<Resource.Animation>(TRANSITION_ANIMATION)
                Resource.Transition.KEY -> bundle.getParcelable<Resource.Transition>(TRANSITION_ANIMATION)
                else -> null
            }
        }
    }

    fun toAnimation(): Resource.Animation? {
        return this as? Resource.Animation
    }

    fun toOpenTransition(): TransitionAnimation? {
        return when (this) {
            is Resource.Transition -> toValue(AppNavigator.context).toOpenTransitionAnimation()
            is TransitionValue -> toOpenTransitionAnimation()
            else -> null
        }
    }

    fun toCloseTransition(): TransitionAnimation? {
        return when (this) {
            is Resource.Transition -> toValue(AppNavigator.context).toExitTransitionAnimation()
            is TransitionValue -> toExitTransitionAnimation()
            else -> null
        }
    }

    fun toActivityAnimation(): ActivityTransitionAnimation? {
        return when (this) {
            is Resource.Transition -> ActivityTransitionAnimation(openEnter, openExit)
            else -> null
        }
    }

    fun saveAnimationData(bundle: Bundle) {
        when (this) {
            is Resource.Animation -> {
                bundle.putString(TRANSITION_ANIMATION_TYPE, Resource.Animation.KEY)
                bundle.putParcelable(TRANSITION_ANIMATION, this)
            }
            is Resource.Transition -> {
                bundle.putString(TRANSITION_ANIMATION_TYPE, Resource.Transition.KEY)
                bundle.putParcelable(TRANSITION_ANIMATION, this)
            }
            is Default -> bundle.putString(TRANSITION_ANIMATION_TYPE, KEY)
            is Fade -> bundle.putString(TRANSITION_ANIMATION_TYPE, KEY)
            is Modal -> bundle.putString(TRANSITION_ANIMATION_TYPE, KEY)
            is Slide -> bundle.putString(TRANSITION_ANIMATION_TYPE, KEY)
            else -> return
        }
    }

    fun applyBeforeFragmentTransaction(
        transaction: FragmentTransaction,
        sourceFragment: Fragment?,
        destinationFragment: Fragment
    ) {
        toOpenTransition()?.applyBeforeFragmentTransaction(transaction, sourceFragment, destinationFragment)
    }

    sealed class Resource: AnimationData(), Parcelable {

        @Parcelize data class Animation(
            @AnimRes val openEnter: Int,
            @AnimRes val openExit: Int,
            @AnimRes val closeEnter: Int,
            @AnimRes val closeExit: Int,
        ): Resource() {

            companion object {
                const val KEY = "custom_resource_animation"
            }

            fun setCustomAnimation(transaction: FragmentTransaction): FragmentTransaction {
                return transaction.setCustomAnimations(
                    openEnter,
                    openExit,
                    closeEnter,
                    closeExit
                )
            }
        }

        @Parcelize data class Transition(
            @TransitionRes val openEnter: Int,
            @TransitionRes val openExit: Int,
            @TransitionRes val popEnter: Int,
            @TransitionRes val popExit: Int,
        ): Resource() {

            companion object {
                const val KEY = "custom_resource_transition"
            }

            fun toValue(context: Context): TransitionValue {
                val inflater = TransitionInflater.from(context)
                return TransitionValue(
                    openEnter = inflater.inflateTransition(openEnter),
                    openExit = inflater.inflateTransition(openExit),
                    popEnter = inflater.inflateTransition(popEnter),
                    popExit = inflater.inflateTransition(popExit),
                )
            }
        }

    }

    object Slide: TransitionValue(
        openEnter = Slide(Gravity.END)
            .setDuration(200L)
            .setStartDelay(50L),
        openExit = TransitionSet().apply {
            addTransition(Slide(Gravity.START))
            addTransition(Fade())
            duration = 200L
        },
        popEnter = Slide(Gravity.START).setDuration(200L),
        popExit = Slide(Gravity.END).setDuration(200L),
    ) {
        const val KEY = "slide"
    }

    object Modal: TransitionValue(
        openEnter = TransitionSet().apply {
            addTransition(Slide(Gravity.BOTTOM))
            addTransition(Fade())
        },
        openExit = AutoTransition(),
        popEnter = AutoTransition(),
        popExit = TransitionSet().apply {
            addTransition(Slide(Gravity.BOTTOM))
            addTransition(Fade())
        },
    ) {
        const val KEY = "modal"
    }

    object Fade: TransitionValue(
        openEnter = Fade(),
        openExit = Fade(),
        popEnter = Fade(),
        popExit = Fade(),
    ) {
        const val KEY = "fade"
    }

    object Default: TransitionValue(
        openEnter = Explode(),
        openExit = AutoTransition(),
        popEnter = Explode(),
        popExit = AutoTransition()
    ) {
        const val KEY = "default"
    }

    open class TransitionValue(
        val openEnter: Transition,
        val openExit: Transition,
        val popEnter: Transition,
        val popExit: Transition,
    ): AnimationData() {
        fun toOpenTransitionAnimation(): TransitionAnimation {
            return SimpleTransitionAnimation(openEnter, openExit)
        }

        fun toExitTransitionAnimation(): TransitionAnimation {
            return SimpleTransitionAnimation(popEnter, popExit)
        }
    }

}
