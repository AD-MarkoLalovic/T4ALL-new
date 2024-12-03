package com.mobility.enp

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mobility.enp.view.fragments.HomeFragment
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeFragmentInstrumentedTest {


    @Test
    fun testProgressBarIsDisplayed() {
        // Check if the progress bar is displayed
        onView(withId(R.id.progBar)).check(matches(isDisplayed()))
    }


    @Test
    fun testPromotionAdapterHasMoreThenOneElement() {
        // Launch the HomeFragment
        val scenario = launchFragmentInContainer<HomeFragment>()

        scenario.onFragment { fragment ->
            // Get the RecyclerView
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.cyclerPromotions)
            assertTrue(recyclerView != null)
            // Check if the adapter has more than one item
            val itemCount = recyclerView?.adapter?.itemCount
            assertTrue(itemCount != null && itemCount > 1)
        }
    }

    @Test
    fun testBillAdapterHasOneElement() {
        // Launch the HomeFragment
        val scenario = launchFragmentInContainer<HomeFragment>()

        scenario.onFragment { fragment ->

            fragment.homeUserData.observeForever {
                // Get the RecyclerView
                val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.cyclerPromotions)
                assertTrue(recyclerView != null)
                // Check if the adapter has more than one item

                val adapter = recyclerView?.adapter
                assertTrue("adapter should not be null", adapter != null)

                val count = recyclerView?.adapter?.itemCount
                assertTrue("array should not be more then one", count == 1)

            }

        }
    }


}