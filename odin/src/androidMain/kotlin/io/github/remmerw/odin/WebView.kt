package io.github.remmerw.odin

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.remmerw.odin.LoadingState.Finished
import io.github.remmerw.odin.LoadingState.Loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WebView(
    modifier: Modifier,
    state: WebViewState,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onInitPage: () -> (String),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() }
) {
    val webView = state.webView

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            with(navigator) {
                wv.handleNavigationEvents()
            }
        }

    }

    // Set the state of the client and chrome client
    // This is done internally to ensure they always are the same instance as the
    // parent Web composable
    client.state = state
    client.navigator = navigator
    chromeClient.state = state


    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                onCreated(this)

                this.layoutParams = FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )

                state.viewState?.let {
                    this.restoreState(it)
                }


                @SuppressLint("SetJavaScriptEnabled")
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = false

                settings.safeBrowsingEnabled = true
                settings.allowContentAccess = false
                settings.allowFileAccess = false
                settings.loadsImagesAutomatically = true
                settings.blockNetworkLoads = false
                settings.blockNetworkImage = false
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.mediaPlaybackRequiresUserGesture = true
                settings.setSupportMultipleWindows(false)
                settings.setGeolocationEnabled(false)

                webChromeClient = chromeClient
                webViewClient = client

                loadUrl(onInitPage.invoke())

            }.also { state.webView = it }
        },
        onRelease = {
            state.webView!!.destroy()
            onDispose(it)
        }
    )
}

/**
 * AccompanistWebViewClient
 *
 * A parent class implementation of WebViewClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be override if further custom behaviour is required.
 */
open class AccompanistWebViewClient : WebViewClient() {
    open lateinit var state: WebViewState
    open lateinit var navigator: WebViewNavigator

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        state.loadingState = LoadingState.Initializing
        state.pageTitle = null
        state.pageIcon = null
        state.lastLoadedUrl = url
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        state.loadingState = Finished
        state.progress = 0.0f
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)

        navigator.canGoBack = view.canGoBack()
        navigator.canGoForward = view.canGoForward()
    }

}

/**
 * AccompanistWebChromeClient
 *
 * A parent class implementation of WebChromeClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be override if further custom behaviour is required.
 */
open class AccompanistWebChromeClient : WebChromeClient() {
    open lateinit var state: WebViewState

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        state.pageTitle = title
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap) {
        super.onReceivedIcon(view, icon)
        state.pageIcon = icon
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)

        if (state.loadingState == Finished) return
        state.loadingState = Loading
        state.progress = newProgress / 100.0f
    }
}


/**
 * Sealed class for constraining possible loading states.
 * See [Loading] and [Finished].
 */

enum class LoadingState {
    Initializing, Loading, Finished
}

/**
 * A state holder to hold the state for the WebView. In most cases this will be remembered
 * using the rememberWebViewState(uri) function.
 */
@Stable
class WebViewState() {
    var lastLoadedUrl: String? by mutableStateOf(null)

    /**
     * Whether the WebView is currently [LoadingState.Loading] data in its main frame (along with
     * progress) or the data loading has [LoadingState.Finished]. See [LoadingState]
     */
    var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)

    var progress: Float by mutableFloatStateOf(0.0f)

    /**
     * Whether the webview is currently loading data in its main frame
     */
    val isLoading: Boolean
        get() = loadingState != Finished


    /**
     * The title received from the loaded content of the current page
     */
    var pageTitle: String? by mutableStateOf(null)

    /**
     * the favicon received from the loaded content of the current page
     */
    var pageIcon: Bitmap? by mutableStateOf(null)

    /**
     * The saved view state from when the view was destroyed last. To restore state,
     * use the navigator and only call loadUrl if the bundle is null.
     * See WebViewSaveStateSample.
     */
    var viewState: Bundle? = null

    // We need access to this in the state saver. An internal DisposableEffect or AndroidView
    // onDestroy is called after the state saver and so can't be used.
    internal var webView by mutableStateOf<WebView?>(null)
}

/**
 * Allows control over the navigation of a WebView from outside the composable. E.g. for performing
 * a back navigation in response to the user clicking the "up" button in a TopAppBar.
 *
 * @see [rememberWebViewNavigator]
 */
@Stable
class WebViewNavigator(private val coroutineScope: CoroutineScope) {
    private sealed interface NavigationEvent {
        object Back : NavigationEvent
        object Forward : NavigationEvent
        object Reload : NavigationEvent
        object StopLoading : NavigationEvent

        data class LoadUrl(
            val url: String,
        ) : NavigationEvent

    }

    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow(replay = 1)

    // Use Dispatchers.Main to ensure that the webview methods are called on UI thread
    internal suspend fun WebView.handleNavigationEvents(): Nothing = withContext(Dispatchers.Main) {
        navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.Back -> goBack()
                is NavigationEvent.Forward -> goForward()
                is NavigationEvent.Reload -> reload()
                is NavigationEvent.StopLoading -> stopLoading()

                is NavigationEvent.LoadUrl -> {
                    loadUrl(event.url)
                }
            }
        }
    }

    /**
     * True when the web view is able to navigate backwards, false otherwise.
     */
    var canGoBack: Boolean by mutableStateOf(false)

    /**
     * True when the web view is able to navigate forwards, false otherwise.
     */
    var canGoForward: Boolean by mutableStateOf(false)

    fun loadUrl(url: String) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadUrl(
                    url
                )
            )
        }
    }

    /**
     * Navigates the webview back to the previous page.
     */
    fun navigateBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Back) }
    }

    /**
     * Navigates the webview forward after going back from a page.
     */
    fun navigateForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Forward) }
    }

    /**
     * Reloads the current page in the webview.
     */
    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Reload) }
    }

    /**
     * Stops the current page load (if one is loading).
     */
    fun stopLoading() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.StopLoading) }
    }
}

/**
 * Creates and remembers a [WebViewNavigator] using the default [CoroutineScope] or a provided
 * override.
 */
@Composable
fun rememberWebViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): WebViewNavigator = remember(coroutineScope) { WebViewNavigator(coroutineScope) }


/**
 * Creates a WebView state that is remembered across Compositions and saved
 * across activity recreation.
 * When using saved state, you cannot change the URL via recomposition. The only way to load
 * a URL is via a WebViewNavigator.
 *
 */
@Composable
fun rememberSaveableWebViewState(): WebViewState =
    rememberSaveable(saver = WebStateSaver) {
        WebViewState()
    }

val WebStateSaver: Saver<WebViewState, Any> = run {
    val pageTitleKey = "pageTitle"
    val lastLoadedUrlKey = "lastLoadedUrl"
    val stateBundle = "bundle"

    mapSaver(
        save = {
            val viewState = Bundle().apply { it.webView?.saveState(this) }
            mapOf(
                pageTitleKey to it.pageTitle,
                lastLoadedUrlKey to it.lastLoadedUrl,
                stateBundle to viewState
            )
        },
        restore = {
            WebViewState().apply {
                this.pageTitle = it[pageTitleKey] as String?
                this.lastLoadedUrl = it[lastLoadedUrlKey] as String?
                this.viewState = it[stateBundle] as Bundle?
            }
        }
    )
}
