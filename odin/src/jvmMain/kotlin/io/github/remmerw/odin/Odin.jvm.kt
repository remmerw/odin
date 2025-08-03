package io.github.remmerw.odin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.remmerw.asen.bootstrap
import io.github.remmerw.idun.Idun
import io.github.remmerw.idun.Storage
import io.github.remmerw.idun.newIdun
import io.github.remmerw.idun.newStorage
import io.github.remmerw.odin.core.Files
import io.github.remmerw.odin.core.Peers
import java.io.File
import kotlin.time.measureTime

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

    val time = measureTime {
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

    println("App started " + time.inWholeMilliseconds)
}
