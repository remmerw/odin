package io.github.remmerw.odin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.remmerw.odin.Homepage
import io.github.remmerw.odin.core.StateModel
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.access_point_network
import io.github.remmerw.odin.generated.resources.app_name
import io.github.remmerw.odin.generated.resources.connections
import io.github.remmerw.odin.generated.resources.documentation
import io.github.remmerw.odin.generated.resources.files
import io.github.remmerw.odin.generated.resources.home
import io.github.remmerw.odin.generated.resources.homepage
import io.github.remmerw.odin.generated.resources.lan_connect
import io.github.remmerw.odin.generated.resources.no_activity_found_to_handle_uri
import io.github.remmerw.odin.generated.resources.plus_thick
import io.github.remmerw.odin.generated.resources.relays
import io.github.remmerw.odin.generated.resources.reset
import io.github.remmerw.odin.generated.resources.share
import io.github.remmerw.odin.generated.resources.untitled
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.sink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.writeString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun MainView(stateModel: StateModel) {

    fun loadUrisFile(uris: List<PlatformFile>) {

        if (uris.isNotEmpty()) {

            val file = PlatformFile(FileKit.cacheDir, Uuid.toString())
            // Get a raw sink for the file (overwrites existing file)
            val sink = file.sink(append = false).buffered()

            // Use the sink to write in chunks
            sink.use { bufferedSink ->
                for (uri in uris) {
                    bufferedSink.writeString(uri.absolutePath() + "\n")
                }
            }
            stateModel.uploadFiles(file.absolutePath())
        }
    }


    val launcher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple()
    ) { files ->
        if (files != null) {
            loadUrisFile(files)
        }
    }

    var showResetDialog: Boolean by remember { mutableStateOf(false) }
    var showMenu: Boolean by remember { mutableStateOf(false) }
    var showRelays: Boolean by remember { mutableStateOf(false) }
    var showConnections: Boolean by remember { mutableStateOf(false) }
    var showInfo: Boolean by remember { mutableStateOf(false) }
    var showHomepage: Boolean by remember { mutableStateOf(false) }


    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val fileInfos by stateModel.fileInfos().collectAsState(emptyList())
    val numRelays by stateModel.numRelays.collectAsState(0)
    val numConnections by stateModel.numConnections.collectAsState(0)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    val onRefresh: () -> Unit = {
        isRefreshing = true
        scope.launch {
            stateModel.makeReservations()
            delay(1500)
            isRefreshing = false
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.app_name)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = stringResource(Res.string.home)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val link = stateModel.pageUri()
                                stateModel.sharePageUri(link)

                            } catch (_: Throwable) {
                                snackbarHostState.showSnackbar(
                                    message = getString(Res.string.no_activity_found_to_handle_uri),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(Res.string.share)
                        )
                    }
                    IconButton(onClick = {
                        showMenu = true
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(Res.string.untitled)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false })
                    {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(Res.string.documentation)) },

                            onClick = {
                                scope.launch {
                                    try {
                                        uriHandler.openUri("https://gitlab.com/lp2p/odin")
                                    } catch (_: Throwable) {
                                        snackbarHostState.showSnackbar(
                                            message = getString(Res.string.no_activity_found_to_handle_uri),
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Indefinite
                                        )
                                    } finally {
                                        showMenu = false
                                    }
                                }
                            })

                        DropdownMenuItem(
                            text = { Text(text = stringResource(Res.string.reset)) },

                            onClick = {
                                showResetDialog = true
                                showMenu = false
                            })
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {

                    BadgedBox(
                        badge = {
                            Badge {
                                Text(text = numRelays.toString())
                            }
                        },
                        modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp)
                    ) {
                        IconButton(onClick = { showRelays = true }) {
                            Icon(
                                painter = painterResource(Res.drawable.access_point_network),
                                contentDescription = stringResource(Res.string.relays)
                            )
                        }
                    }

                    BadgedBox(
                        badge = {
                            Badge {
                                Text(text = numConnections.toString())
                            }
                        },
                        modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp)
                    ) {
                        IconButton(onClick = { showConnections = true }) {
                            Icon(
                                painterResource(Res.drawable.lan_connect),
                                contentDescription = stringResource(Res.string.connections)
                            )
                        }
                    }

                    IconButton(onClick = { showHomepage = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Preview,
                            contentDescription = stringResource(Res.string.homepage)
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { launcher.launch() },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.plus_thick),
                            contentDescription = stringResource(Res.string.files)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->

        val listState = rememberLazyListState()
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {

            Text(
                text = stringResource(stateModel.reachability),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colorScheme.surface)
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
                onRefresh = onRefresh
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    items(items = fileInfos, key = { fileItem ->
                        fileItem.idx
                    }) { fileInfo ->
                        SwipeFileInfoItem(stateModel, fileInfo)
                    }
                }
            }

            if (isRefreshing) {
                LaunchedEffect(Unit) {
                    stateModel.makeReservations()
                    delay(1000)
                    isRefreshing = false
                }
            }
        }
    }


    if (showRelays) {
        ModalBottomSheet(onDismissRequest = { showRelays = false }) {
            RelaysView(stateModel.reservations())
        }
    }

    if (showConnections) {
        ModalBottomSheet(onDismissRequest = { showConnections = false }) {
            ConnectionsView(stateModel.incomingConnections())
        }
    }

    if (showInfo) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showInfo = false }) {
            InfoView(stateModel)
        }
    }

    if (stateModel.homepageImplemented()) {
        if (showHomepage) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
            ModalBottomSheet(
                containerColor = Color.Black.copy(0.3f),
                sheetState = sheetState,
                modifier = Modifier
                    .fillMaxHeight()
                    .safeDrawingPadding(),
                onDismissRequest = { showHomepage = false }) {
                Homepage(stateModel)
            }
        }
    }

    if (showResetDialog) {
        ResetDialog(
            stateModel = stateModel,
            onDismissRequest = {
                showResetDialog = false
            })
    }
}