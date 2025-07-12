package io.github.remmerw.odin

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.remmerw.asen.bootstrap
import io.github.remmerw.idun.Idun
import io.github.remmerw.idun.Storage
import io.github.remmerw.idun.newIdun
import io.github.remmerw.idun.newStorage
import io.github.remmerw.odin.core.FileInfo
import io.github.remmerw.odin.core.Files
import io.github.remmerw.odin.core.Peers
import io.github.remmerw.odin.core.StateModel
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.UUID

private var odin: Odin? = null

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class Context
object JvmContext : Context()

internal class JvmOdin(
    private val files: Files,
    private val storage: Storage,
    private val idun: Idun,
    private val peers: Peers
) : Odin() {

    override suspend fun sharePageUri(uri: String) {
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            val mailto = URI("mailto:?subject=$uri?body=$uri")
            desktop.mail(mailto)
        } else {
            throw Exception("no mail program")
        }
    }

    override fun homepageImplemented(): Boolean {
        return false
    }


    override fun cancelWork(fileInfo: FileInfo) {
        workUUID(fileInfo) ?: return // todo cancel the work
    }

    override fun uploadFiles(absolutePath: String) {
        //val job = thread {

        //}
        //UploadFilesWorker.load(context, name) // todo
    }

    override fun deviceName(): String {
        val os = System.getProperty("os.name")
        val version = System.getProperty("os.version")
        return "$os $version"
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


@Composable
actual fun Homepage(stateModel: StateModel) {
}

actual fun odin(): Odin = odin!!

private fun databasePeers(): RoomDatabase.Builder<Peers> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "peers.db")
    return Room.databaseBuilder<Peers>(
        name = dbFile.absolutePath,
    )
}

private fun createPeers(): Peers {
    return peersDatabaseBuilder(databasePeers())
}


private fun databaseFiles(): RoomDatabase.Builder<Files> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "files.db")
    return Room.databaseBuilder<Files>(
        name = dbFile.absolutePath,
    )
}

private fun createFiles(): Files {
    return filesDatabaseBuilder(databaseFiles())
}

private fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        File(System.getProperty("java.io.tmpdir"))
            .resolve(dataStoreFileName).absolutePath
    }
)


actual fun initializeOdin(context: Context) {
    val datastore = createDataStore()
    val files = createFiles()
    val peers = createPeers()


    val storage = newStorage()
    val idun = newIdun(
        keys = keys(datastore),
        bootstrap = bootstrap(),
        peerStore = peers
    )

    odin = JvmOdin(files, storage, idun, peers)
}
