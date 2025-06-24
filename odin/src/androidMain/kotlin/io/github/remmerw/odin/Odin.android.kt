package io.github.remmerw.odin

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.WorkManager
import com.eygraber.uri.Uri
import io.github.remmerw.asen.PeerId
import io.github.remmerw.asen.Peeraddr
import io.github.remmerw.asen.decode58
import io.github.remmerw.idun.Event
import io.github.remmerw.idun.Idun
import io.github.remmerw.idun.Storage
import io.github.remmerw.idun.newIdun
import io.github.remmerw.idun.newStorage
import io.github.remmerw.odin.core.CONTENT_DOWNLOAD
import io.github.remmerw.odin.core.FileInfo
import io.github.remmerw.odin.core.Files
import io.github.remmerw.odin.core.MimeType
import io.github.remmerw.odin.core.Peers
import io.github.remmerw.odin.core.StateModel
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.no_activity_found_to_handle_uri
import io.github.remmerw.odin.generated.resources.share
import io.github.remmerw.odin.generated.resources.share_link
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.manualFileKitCoreInitialization
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.UUID
import kotlin.time.measureTime

private var odin: Odin? = null

internal class AndroidOdin(
    private val context: Context,
    private val datastore: DataStore<Preferences>,
    private val files: Files,
    private val storage: Storage,
    private val idun: Idun,
    private val peers: Peers
) : Odin() {


    override suspend fun sharePageUri(uri: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(
            Intent.EXTRA_SUBJECT, getString(Res.string.share_link)
        )
        intent.putExtra(Intent.EXTRA_TEXT, uri)
        intent.setType(MimeType.PLAIN_MIME_TYPE.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(
            intent, getString(Res.string.share)
        )

        chooser.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)

    }

    override fun homepageImplemented(): Boolean {
        return true
    }

    override fun cancelWork(fileInfo: FileInfo) {
        val uuid = workUUID(fileInfo) ?: return
        WorkManager
            .getInstance(context)
            .cancelWorkById(uuid)
    }

    override fun uploadFiles(absolutePath: String) {
        UploadFilesWorker.Companion.load(context, absolutePath)
    }

    override fun reservationFeaturePossible(): Boolean {
        return publicAddresses().isNotEmpty()
    }

    override fun deviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer)) {
            return model
        }
        return "$manufacturer $model"
    }

    override fun peeraddrs(): List<Peeraddr> {
        return peeraddrs(idun().peerId())
    }

    override fun datastore(): DataStore<Preferences> {
        return datastore
    }

    override fun files(): Files {
        return files
    }

    override fun peers(): Peers {
        return peers
    }

    override fun storage(): Storage {
        return storage
    }

    override fun idun(): Idun {
        return idun
    }
}

private fun workUUID(fileInfo: FileInfo): UUID? {
    if (fileInfo.work != null) {
        return UUID.fromString(fileInfo.work)
    }
    return null
}


private fun isLanAddress(inetAddress: InetAddress): Boolean {
    return inetAddress.isAnyLocalAddress
            || inetAddress.isLinkLocalAddress
            || (inetAddress.isLoopbackAddress)
            || (inetAddress.isSiteLocalAddress)
}

private fun publicAddresses(): List<InetAddress> {
    val inetAddresses: MutableList<InetAddress> = ArrayList()

    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()

        for (networkInterface in interfaces) {
            if (networkInterface.isUp) {
                val addresses = networkInterface.inetAddresses
                for (inetAddress in addresses) {
                    if (inetAddress is Inet6Address) {
                        if (!isLanAddress(inetAddress)) {
                            inetAddresses.add(inetAddress)
                        }
                    }
                }
            }
        }
    } catch (throwable: Throwable) {
        throw IllegalStateException(throwable)
    }
    return inetAddresses
}

private fun peeraddrs(peerId: PeerId): List<Peeraddr> {
    val inetSocketAddresses: Collection<InetAddress> = publicAddresses()
    val result = mutableListOf<Peeraddr>()
    for (inetAddress in inetSocketAddresses) {
        result.add(Peeraddr(peerId, inetAddress.address, ODIN_PORT.toUShort()))
    }
    return result
}

@Composable
actual fun Homepage(stateModel: StateModel) {
    val webViewNavigator = rememberWebViewNavigator()
    val webViewState = rememberSaveableWebViewState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            if (webViewNavigator.canGoBack) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        webViewNavigator.navigateBack()
                    }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(android.R.string.untitled)
                    )
                }
            }
        },
        content = { padding ->
            WebView(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                state = webViewState,
                navigator = webViewNavigator,
                client = remember {
                    CustomWebViewClient { warning ->
                        scope.launch {
                            Toast.makeText(
                                context, getString(warning),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onInitPage = {
                    stateModel.pageUri()
                },
                onCreated = { view ->

                })
        })

}


private class CustomWebViewClient(val warning: (StringResource) -> Unit) :
    AccompanistWebViewClient() {


    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {

        val uri = request.url

        when (uri.scheme) {
            "pns" -> {
                val name = uri.getQueryParameter(CONTENT_DOWNLOAD)
                if (name != null) {
                    warning.invoke(Res.string.no_activity_found_to_handle_uri)
                    return true
                }
            }
        }

        return false
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val uri = Uri.parse(request.url.toString())


        if (uri.scheme == "pns") {
            val peerId = extractPeerIdFromUri(uri)
            checkNotNull(peerId)
            val cid = extractCidFromUri(uri)
            val response = odin().storage().response(cid)
            return WebResourceResponse(
                response.mimeType, response.encoding,
                response.status, response.reason,
                response.headers, Stream(response.channel)
            )
        }
        return null
    }
}

internal fun extractPeerIdFromUri(pns: Uri): PeerId {
    val host = validate(pns)
    return PeerId(decode58(host))
}

internal fun validate(pns: Uri): String {
    checkNotNull(pns.scheme) { "Scheme not defined" }
    require(pns.scheme == "pns") { "Scheme not pns" }
    val host = pns.host
    checkNotNull(host) { "Host not defined" }
    require(host.isNotBlank()) { "Host is empty" }
    return host
}

internal fun extractCidFromUri(pns: Uri): Long? {
    var path = pns.path
    if (path != null) {
        path = path.trim().removePrefix("/")
        if (path.isNotBlank()) {
            val cid = path.toLong(radix = 16)
            return cid
        }
    }
    return null
}


actual fun odin(): Odin = odin!!

private fun databasePeers(ctx: Context): RoomDatabase.Builder<Peers> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("peers.db")
    return Room.databaseBuilder<Peers>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

private fun createPeers(ctx: Context): Peers {
    return peersDatabaseBuilder(databasePeers(ctx))
}


private fun databaseFiles(ctx: Context): RoomDatabase.Builder<Files> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("files.db")
    return Room.databaseBuilder<Files>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

private fun createFiles(ctx: Context): Files {
    return filesDatabaseBuilder(databaseFiles(ctx))
}

private fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)


fun initializeOdin(context: Context) {

    val time = measureTime {
        // Initialize FileKit
        FileKit.manualFileKitCoreInitialization(context)

        val datastore = createDataStore(context)
        val files = createFiles(context)
        val peers = createPeers(context)
        val path = Path(context.filesDir.absolutePath, "storage")
        if (!SystemFileSystem.exists(path)) {
            SystemFileSystem.createDirectories(path, true)
        }

        val storage = newStorage(path)
        val idun = newIdun(
            keys = keys(datastore),
            events = { event: Event ->
                if (event == Event.INCOMING_CONNECT_EVENT) {
                    runBlocking { // todo is this smart ???
                        odin!!.numIncomingConnections()
                    }
                }
                if (event == Event.OUTGOING_RESERVE_EVENT) {
                    runBlocking { // todo is this smart ???
                        odin!!.numRelays()
                    }
                }
            },
            peerStore = peers
        )

        odin = AndroidOdin(context, datastore, files, storage, idun, peers)




        kotlinx.coroutines.MainScope().launch {

            odin!!.initPage()
            odin!!.runService()

            delay(5000) // 5 sec initial delay
            while (isActive) {
                odin!!.makeReservations()
                delay((60 * 30 * 1000).toLong()) // 30 min
            }
        }
    }

    Log.e("App", "App started " + time.inWholeMilliseconds)
}
