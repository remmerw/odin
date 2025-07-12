package io.github.remmerw.odin

import android.os.Build
import android.util.Log
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
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.measureTime

private var odin: Odin? = null

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias Context = android.content.Context

internal class AndroidOdin(
    private val files: Files,
    private val storage: Storage,
    private val idun: Idun,
    private val peers: Peers
) : Odin() {


    override fun deviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer)) {
            return model
        }
        return "$manufacturer $model"
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


actual fun initializeOdin(context: Context) {

    val time = measureTime {


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
            bootstrap = bootstrap(),
            peerStore = peers
        )

        odin = AndroidOdin(files, storage, idun, peers)
    }

    Log.e("App", "App started " + time.inWholeMilliseconds)
}
