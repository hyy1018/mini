// #member1
// Double-tap-safe navigation. A quick double tap fires onClick twice before the screen changes:
// popBackStack() twice can empty the back stack and leave a blank screen, and navigate() twice
// opens the same page twice. Only the screen that is fully shown (RESUMED) may navigate,
// so the second tap is ignored while the first transition is still running.
package com.example.mini.nav

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

fun NavHostController.isCurrentScreenResumed(): Boolean =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

fun NavHostController.safeNavigate(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    if (isCurrentScreenResumed()) navigate(route, builder)
}

// never pops the last screen, so the app can't end up on an empty NavHost
fun NavHostController.safePopBackStack() {
    if (isCurrentScreenResumed() && previousBackStackEntry != null) popBackStack()
}
