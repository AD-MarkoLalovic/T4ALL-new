package com.mobility.enp.util

import android.view.Gravity
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T> Fragment.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        //repeatOnLifecycle osigurava da prikupljanje počinje samo kada je Fragment u STARTED stanju ili aktivnijem stanju (RESUMED)
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun DialogFragment.setDimensionsPercent(widthPercent: Int, heightPercent: Int? = null) {
    val metrics = requireContext().resources.displayMetrics
    val width = (metrics.widthPixels * (widthPercent / 100f)).toInt()
    val height = heightPercent?.let {
        (metrics.heightPixels * (it / 100f)).toInt()
    } ?: ViewGroup.LayoutParams.WRAP_CONTENT

    dialog?.window?.apply {
        setLayout(width, height)
        setGravity(Gravity.CENTER)
    }

}

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

fun Fragment.safeNavigate(directions: NavDirections, @IdRes fromDestinationId: Int) {

    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return

    val navController = findNavController()

    if (navController.currentDestination?.id != fromDestinationId) return

    val actionExists = navController.currentDestination?.getAction(directions.actionId)
        ?: navController.graph.getAction(directions.actionId)

    if (actionExists != null) {
        navController.navigate(directions)
    }
}
