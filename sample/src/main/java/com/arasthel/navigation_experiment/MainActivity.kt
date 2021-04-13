package com.arasthel.navigation_experiment

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import com.arasthel.navigation.*
import com.arasthel.navigation.annotations.RegisterScreen
import com.arasthel.navigation.base.NavigationActivity
import com.arasthel.navigation.base.NavigationFragment
import com.arasthel.navigation.base.getScreen
import com.arasthel.navigation.navigators.fade
import com.arasthel.navigation.navigators.horizontal
import com.arasthel.navigation.navigators.noAnimation
import com.arasthel.navigation.navigators.vertical
import com.arasthel.navigation.properties.activityNavigator
import com.arasthel.navigation.properties.childNavigator
import com.arasthel.navigation.properties.parentNavigator
import com.arasthel.navigation.properties.registerForResult
import com.arasthel.navigation.screen.Screen
import com.arasthel.navigation.screen.ScreenResult
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

object MainScreen: Screen

@RegisterScreen(MainScreen::class)
class MainActivity : NavigationActivity(R.layout.activity_main) {

    private val testResult by registerForResult<TestResult> {
        Toast.makeText(this, "Activity Result: ${it.id}", Toast.LENGTH_SHORT).show()
    }

    private val navigator by activityNavigator()
    private val childNavigator by childNavigator(R.id.navContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            childNavigator.push(TestFragmentScreen(TestFragment.count).horizontal())
            TestFragment.count++
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
//            childNavigator.pop()
//            TestFragment.count--
            navigator.push(OtherScreen("ASDA").noAnimation())
        }

//        val switcherNavigator = bindSwitcherNavigator(R.id.navContainer)
//
//        findViewById<Button>(R.id.nextButton).setOnClickListener {
//            switcherNavigator.switchTo(TestFragmentScreen(1))
//        }
//
//        findViewById<Button>(R.id.backButton).setOnClickListener {
//            switcherNavigator.switchTo(TestFragmentScreen(2))
//        }
    }

    override fun onResume() {
        super.onResume()

        if (!childNavigator.hasRootFragment()) {
            childNavigator.push(TestFragmentScreen(TestFragment.count).horizontal())
            TestFragment.count++
        }
    }

}

@Parcelize data class TestFragmentScreen(val count: Int): Screen, Parcelable
@Parcelize data class TestResult(val id: Int): ScreenResult, Parcelable

@RegisterScreen(TestFragmentScreen::class)
class TestFragment: NavigationFragment(R.layout.fragment_first) {

    companion object {
        var count = 1
    }

    val color = Color.HSVToColor(floatArrayOf(Random.nextInt(360).toFloat(), 0.1f, 0.8f))

    private val resultObserver by registerForResult<TestResult> { result ->
        activity?.let { Toast.makeText(it, "Fragment Result: ${result.id}", Toast.LENGTH_SHORT).show() }
    }

    private val navigator by parentNavigator()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        view.setBackgroundColor(color)

        val screen = getScreen<TestFragmentScreen>()
        view.findViewById<TextView>(R.id.text).text = "Fragment #${screen.count}"
    }

    override fun onResume() {
        super.onResume()

        view?.setOnClickListener {
//            getParentNavigator()?.popWithResult(TestResult(Random.nextInt()))
//            count--
            navigator?.reset(OtherScreen("HEHE").fade())
        }
    }

}