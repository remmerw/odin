package io.github.remmerw.odin

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.remmerw.asen.Peeraddr
import io.github.remmerw.idun.Event
import io.github.remmerw.idun.Idun
import io.github.remmerw.idun.Storage
import io.github.remmerw.idun.newIdun
import io.github.remmerw.idun.newStorage
import io.github.remmerw.odin.core.FileInfo
import io.github.remmerw.odin.core.Files
import io.github.remmerw.odin.core.Peers
import io.github.remmerw.odin.core.StateModel
import kotlinx.coroutines.runBlocking

private var odin: Odin? = null

internal class IosOdin(
    private val datastore: DataStore<Preferences>,
    private val files: Files,
    private val storage: Storage,
    private val idun: Idun,
    private val peers: Peers
) : Odin() {

    override suspend fun sharePageUri(uri: String) {

    }

    override fun homepageImplemented(): Boolean {
        return false
    }


    override fun cancelWork(fileInfo: FileInfo) {
        // workUUID(fileInfo) ?: return
    }

    override fun uploadFiles(absolutePath: String) {
        //UploadFilesWorker.load(context, name)
    }

    override fun reservationFeaturePossible(): Boolean {
        return false
    }

    override fun deviceName(): String {
        return io.github.remmerw.odin.deviceName()
    }

    override fun peeraddrs(): List<Peeraddr> {
        return emptyList() // todo
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


private fun deviceName(): String { // todo
    val manufacturer = "PC"
    val model = "todo"
    if (model.startsWith(manufacturer)) {
        return model
    }
    return "$manufacturer $model"
}


@Composable
actual fun Homepage(stateModel: StateModel) {
}

actual fun odin(): Odin = odin!!

private fun databasePeers(): RoomDatabase.Builder<Peers> {
    val dbFilePath = documentDirectory() + "/peers.db"
    return Room.databaseBuilder<Peers>(
        name = dbFilePath,
    )
}

private fun createPeers(): Peers {
    return peersDatabaseBuilder(databasePeers())
}


private fun databaseFiles(): RoomDatabase.Builder<Files> {
    val dbFilePath = documentDirectory() + "/files.db"
    return Room.databaseBuilder<Files>(
        name = dbFilePath,
    )
}

private fun createFiles(): Files {
    return filesDatabaseBuilder(databaseFiles())
}

private fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = { documentDirectory() + "/" + dataStoreFileName }
)


private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}


internal fun initializeOdin(): Odin {
    val datastore = createDataStore()
    val files = createFiles()
    val peers = createPeers()


    val storage = newStorage()
    val idun = newIdun(
        keys = keys(datastore),
        events = { event: Event ->
            if (event == Event.INCOMING_CONNECT_EVENT) {
                runBlocking { // todo
                    odin!!.numIncomingConnections()
                }
            }
            if (event == Event.OUTGOING_RESERVE_EVENT) {
                runBlocking { // todo
                    odin!!.numRelays()
                }
            }
        },
        peerStore = peers
    )

    odin = IosOdin(datastore, files, storage, idun, peers)

    return odin()
}
