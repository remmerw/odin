package io.github.remmerw.odin.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.remmerw.odin.core.FileInfo
import io.github.remmerw.odin.core.MimeType
import io.github.remmerw.odin.core.StateModel
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.application
import io.github.remmerw.odin.generated.resources.arrow_up_down_bold
import io.github.remmerw.odin.generated.resources.audio
import io.github.remmerw.odin.generated.resources.camera
import io.github.remmerw.odin.generated.resources.cancel
import io.github.remmerw.odin.generated.resources.file
import io.github.remmerw.odin.generated.resources.file_star
import io.github.remmerw.odin.generated.resources.file_video
import io.github.remmerw.odin.generated.resources.folder
import io.github.remmerw.odin.generated.resources.help
import io.github.remmerw.odin.generated.resources.pdf
import io.github.remmerw.odin.generated.resources.untitled
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SwipeFileInfoItem(stateModel: StateModel, fileInfo: FileInfo) {

    var dismissState: SwipeToDismissBoxState? = null
    val threshold = .5f
    dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && dismissState!!.progress > threshold) {
                stateModel.delete(fileInfo)
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * threshold }
    )


    SwipeToDismissBox(
        modifier = Modifier.animateContentSize(),
        state = dismissState,
        backgroundContent = {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd

            ) {
                Icon(
                    Icons.Filled.Delete,
                    stringResource(Res.string.untitled),
                    modifier = Modifier.minimumInteractiveComponentSize()
                )
            }
        },
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = false,
    ) {
        FileInfoItem(stateModel, fileInfo)
    }

}

@Composable
fun FileInfoItem(stateModel: StateModel, fileInfo: FileInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)

    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            val work = fileInfo.work

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .minimumInteractiveComponentSize()
            ) {
                Icon(
                    painter = painterResource(getMediaResource(fileInfo.mimeType)),
                    contentDescription = stringResource(Res.string.untitled),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
                if (work != null) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1.0f, true)
            ) {
                Text(
                    text = compactString(fileInfo.name), maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium, softWrap = true
                )
                Text(
                    text = fileInfoSize(fileInfo), maxLines = 1,
                    style = MaterialTheme.typography.bodySmall, softWrap = true
                )
            }

            if (work != null) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        stateModel.cancelWork(fileInfo)
                    })
                {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(
                            Res.string.cancel
                        ),
                    )
                }
            }
        }
    }
}


private fun fileInfoSize(fileInfo: FileInfo): String {
    val fileSize: String
    val size = fileInfo.size

    if (size < 1000) {
        fileSize = size.toString()
        return "$fileSize B"
    } else if (size < 1000 * 1000) {
        fileSize = (size / 1000).toDouble().toString()
        return "$fileSize KB"
    } else {
        fileSize = (size / (1000 * 1000)).toDouble().toString()
        return "$fileSize MB"
    }
}

private fun compactString(title: String): String {
    return title.replace("\n", " ")
}


private fun getMediaResource(mimeType: String): DrawableResource {
    if (mimeType.isNotEmpty()) {
        if (mimeType == MimeType.TORRENT_MIME_TYPE.name) {
            return Res.drawable.arrow_up_down_bold
        }
        if (mimeType == MimeType.OCTET_MIME_TYPE.name) {
            return Res.drawable.file_star
        }
        if (mimeType == MimeType.PLAIN_MIME_TYPE.name) {
            return Res.drawable.file
        }
        if (mimeType.startsWith(MimeType.TEXT.name)) {
            return Res.drawable.file
        }
        if (mimeType == MimeType.PDF_MIME_TYPE.name) {
            return Res.drawable.pdf
        }
        if (mimeType == MimeType.DIR_MIME_TYPE.name) {
            return Res.drawable.folder
        }
        if (mimeType.startsWith(MimeType.VIDEO.name)) {
            return Res.drawable.file_video
        }
        if (mimeType.startsWith(MimeType.IMAGE.name)) {
            return Res.drawable.camera
        }
        if (mimeType.startsWith(MimeType.AUDIO.name)) {
            return Res.drawable.audio
        }
        if (mimeType.startsWith(MimeType.APPLICATION.name)) {
            return Res.drawable.application
        }
    }

    return Res.drawable.help
}