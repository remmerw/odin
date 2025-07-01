package io.github.remmerw.odin

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.compose.rememberConnectivityState
import io.github.remmerw.odin.core.Reachability
import io.github.remmerw.odin.core.StateModel
import io.github.remmerw.odin.ui.MainView


@Composable
fun AppTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current

    val lightingColorScheme = lightColorScheme()

    val colorScheme =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (useDarkTheme)
                dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            lightingColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

@Composable
fun App() {

    val stateModel: StateModel = viewModel { StateModel() }


    val state = rememberConnectivityState {
        autoStart = true
    }

    when (state.status) {
        is Connectivity.Status.Connected -> stateModel.reachability = Reachability.UNKNOWN
        is Connectivity.Status.Disconnected -> stateModel.reachability = Reachability.OFFLINE
        else -> {}
    }
    KeepScreenOn()
    AppTheme {
        Box(modifier = Modifier.safeDrawingPadding()) {
            MainView(stateModel)
        }
    }

}