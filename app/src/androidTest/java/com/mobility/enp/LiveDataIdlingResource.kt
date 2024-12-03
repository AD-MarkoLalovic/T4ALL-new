package com.mobility.enp

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.espresso.IdlingResource

class LiveDataIdlingResource(private val liveData: LiveData<*>) : IdlingResource {

    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    private val observer = Observer<Any> {
        if (liveData.value != null) {
            callback?.onTransitionToIdle()
        }
    }

    override fun getName(): String = "LiveDataIdlingResource"

    override fun isIdleNow(): Boolean {
        return liveData.value != null
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
        liveData.observeForever(observer)
    }

    fun cleanup() {
        liveData.removeObserver(observer)
    }
}