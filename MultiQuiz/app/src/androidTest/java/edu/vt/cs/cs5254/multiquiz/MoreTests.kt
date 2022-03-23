package edu.vt.cs.cs5254.multiquiz

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import edu.vt.cs.cs5254.multiquiz.OrientationChangeAction.Companion.orientationLandscape
import edu.vt.cs.cs5254.multiquiz.OrientationChangeAction.Companion.orientationPortrait
import org.hamcrest.core.IsNot.not
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoreTests {

    @get:Rule
    var myActivityRule = ActivityScenarioRule(QuizActivity::class.java)

    @Test
    fun submitButtonInitiallyDisabled() {
        onView(withId(R.id.submit_button))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun submitEnabledAfterClickingAnswer() {
        onView(withId(R.id.answer_1_button))
            .perform(click())
        onView(withId(R.id.submit_button))
            .check(matches((isEnabled())))
    }


    @Test
    fun answer1DisabledAfterHint() {
        onView(withId(R.id.answer_1_button))
            .perform(click())
        onView(withId(R.id.hint_button))
            .perform(click())
        onView(withId(R.id.hint_button))
            .perform(click())
        onView(withId(R.id.hint_button))
            .perform(click())
        onView(withId(R.id.answer_1_button))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun submitDisabledAfterHint() {
        onView(withId(R.id.answer_1_button))
            .perform(click())
        onView(withId(R.id.submit_button))
            .check(matches((isEnabled())))
        onView(withId(R.id.hint_button))
            .perform(click())
        onView(withId(R.id.hint_button))
            .perform(click())
        onView(withId(R.id.hint_button))
            .perform(click())
        onView(withId(R.id.answer_1_button))
            .check(matches(not(isEnabled())))
        onView(withId(R.id.submit_button))
            .check(matches(not(isEnabled())))
    }


}