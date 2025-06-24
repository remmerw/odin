package io.github.remmerw.odin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.remmerw.asen.Keys
import io.github.remmerw.asen.Peeraddr
import io.github.remmerw.asen.generateKeys
import io.github.remmerw.idun.Idun
import io.github.remmerw.idun.Storage
import io.github.remmerw.odin.core.FileInfo
import io.github.remmerw.odin.core.Files
import io.github.remmerw.odin.core.Peers
import io.github.remmerw.odin.core.StateModel
import io.github.remmerw.odin.core.directoryContent
import io.github.remmerw.odin.core.getPrivateKey
import io.github.remmerw.odin.core.setPrivateKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

const val ODIN_PORT: Int = 5001

abstract class Odin {
    var reserveActive: Boolean by mutableStateOf(false)
    var numRelays: Int by mutableIntStateOf(0)
    var numConnections: Int by mutableIntStateOf(0)

    suspend fun initPage() {
        val fileInfos = files().fileInfos()
        val content: String = directoryContent(fileInfos, deviceName())
        storage().root(content.encodeToByteArray())
    }

    suspend fun makeReservations() {
        idun().makeReservations(peeraddrs(), 100, 120) { running ->
            reserveActive = running
        }
    }

    suspend fun runService() {
        idun().runService(storage(), ODIN_PORT)
    }

    suspend fun numIncomingConnections() {
        numConnections = idun().numIncomingConnections()
    }

    suspend fun numRelays() {
        numRelays = idun().numReservations()
    }

    abstract suspend fun sharePageUri(uri: String)
    abstract fun homepageImplemented(): Boolean
    abstract fun cancelWork(fileInfo: FileInfo)
    abstract fun uploadFiles(absolutePath: String)
    abstract fun reservationFeaturePossible(): Boolean
    abstract fun deviceName(): String
    abstract fun peeraddrs(): List<Peeraddr>
    abstract fun datastore(): DataStore<Preferences>
    abstract fun files(): Files
    abstract fun peers(): Peers
    abstract fun storage(): Storage
    abstract fun idun(): Idun
}

@Composable
expect fun Homepage(stateModel: StateModel)

expect fun odin(): Odin

@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FilesConstructor : RoomDatabaseConstructor<Files> {
    override fun initialize(): Files
}


@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PeersConstructor : RoomDatabaseConstructor<Peers> {
    override fun initialize(): Peers
}


fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val dataStoreFileName = "settings.preferences_pb"


fun filesDatabaseBuilder(
    builder: RoomDatabase.Builder<Files>
): Files {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}


fun peersDatabaseBuilder(
    builder: RoomDatabase.Builder<Peers>
): Peers {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

fun keys(datastore: DataStore<Preferences>): Keys {
    return runBlocking {
        val privateKey = getPrivateKey(datastore).first()
        if (privateKey.isNotEmpty()) {
            return@runBlocking generateKeys(privateKey)
        } else {
            val keys = generateKeys()
            setPrivateKey(datastore, keys.privateKey)
            return@runBlocking keys
        }
    }
}



