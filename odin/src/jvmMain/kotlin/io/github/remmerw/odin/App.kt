package io.github.remmerw.odin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.compose.rememberConnectivityState
import io.github.remmerw.odin.core.StateModel
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.audio
import io.github.remmerw.odin.ui.MainView
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import java.awt.Toolkit


@Composable
fun ApplicationScope.App() {

    // Initialize FileKit
    FileKit.init(appId = "Odin")

    Window(
        onCloseRequest = ::exitApplication,
        title = "Odin",
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = getPreferredWindowSize(720, 857)
        ),
        icon = painterResource(Res.drawable.audio), // todo

    ) {
        val odin = initializeOdin() // todo


        MainScope().launch { // todo maybe

            odin.initPage()
            odin.runService()

            delay(5000) // 5 sec initial delay
            while (isActive) {
                odin.makeReservations()
                delay((60 * 30 * 1000).toLong()) // 30 min
            }
        }

        val stateModel: StateModel = viewModel { StateModel() }


        val state = rememberConnectivityState {
            autoStart = true
        }

        when (state.status) {
            is Connectivity.Status.Connected ->  stateModel.reachability(stateModel.evaluateReachability())
            is Connectivity.Status.Disconnected ->  stateModel.reachability(StateModel.Reachability.UNKNOWN)
            else -> {}
        }



        Box(modifier = Modifier.safeDrawingPadding()) {
            MainView(stateModel)
        }
    }
}


private fun getPreferredWindowSize(desiredWidth: Int, desiredHeight: Int): DpSize {
    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    val preferredWidth: Int = (screenSize.width * 0.8f).toInt()
    val preferredHeight: Int = (screenSize.height * 0.8f).toInt()
    val width: Int = if (desiredWidth < preferredWidth) desiredWidth else preferredWidth
    val height: Int = if (desiredHeight < preferredHeight) desiredHeight else preferredHeight
    return DpSize(width.dp, height.dp)
}