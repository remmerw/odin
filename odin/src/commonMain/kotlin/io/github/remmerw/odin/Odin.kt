package io.github.remmerw.odin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.remmerw.asen.Keys
import io.github.remmerw.asen.PeerId
import io.github.remmerw.asen.SocketAddress
import io.github.remmerw.asen.generateKeys
import io.github.remmerw.idun.Idun
import io.github.remmerw.idun.Storage
import io.github.remmerw.idun.pnsUri
import io.github.remmerw.odin.core.FileInfo
import io.github.remmerw.odin.core.Files
import io.github.remmerw.odin.core.Peers
import io.github.remmerw.odin.core.directoryContent
import io.github.remmerw.odin.core.getPrivateKey
import io.github.remmerw.odin.core.getPublicKey
import io.github.remmerw.odin.core.setPrivateKey
import io.github.remmerw.odin.core.setPublicKey
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.io.RawSource
import okio.Path.Companion.toPath

const val ODIN_PORT: Int = 5001

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect abstract class Context

abstract class Odin {

    fun peerId(): PeerId {
        return idun().peerId()
    }

    suspend fun fileNames(): List<String> {
        return files().fileNames()
    }

    suspend fun storeSource(
        source: RawSource, uuid: String, name: String,
        mimeType: String, size: Long
    ) {
        val fileInfo = FileInfo(
            0, name, mimeType,
            0, uuid, size
        )
        val idx = files().storeFileInfo(fileInfo)
        try {
            val node = storage().storeSource(source, name, mimeType)
            files().done(idx, node.cid())
        } catch (throwable: Throwable) {
            files().delete(idx)
            throw throwable
        }
    }

    fun reservations(): List<InetSocketAddress> {
        return idun().reservations()
    }

    fun numReservations(): Int {
        return idun().numReservations()
    }

    fun numIncomingConnections(): Int {
        return idun().numIncomingConnections()
    }

    fun fileInfos(): Flow<List<FileInfo>> {
        return files().filesDao().flowFileInfos()
    }

    fun pageUri(): String {
        return pnsUri(idun().peerId())
    }

    suspend fun delete(fileInfo: FileInfo) {
        // Note: the content itself (in the block store) will not be deleted
        // this is a limitation (idea the user has to cleanup the block store
        // from time to time
        files().filesDao().delete(fileInfo)

        initPage()
    }

    fun incomingConnections(): Set<InetSocketAddress> {
        return idun().incomingConnections()
    }


    suspend fun startup() {
        initPage()
        idun().startup(storage(), ODIN_PORT)
    }

    suspend fun reset() {
        storage().reset()
        files().reset()
        initPage()
    }

    suspend fun observedAddresses(): List<SocketAddress> {
        return idun().observedAddresses(ODIN_PORT)
    }

    suspend fun initPage() {
        val fileInfos = files().fileInfos()
        val content: String = directoryContent(
            idun().peerId(),
            fileInfos, deviceName()
        )
        storage().root(content.encodeToByteArray())
    }

    suspend fun publishPeeraddrs(
        addresses: List<SocketAddress>,
        maxPublifications: Int = 100,
        timeout: Int = 120
    ) {
        idun().publishAddresses(addresses, maxPublifications, timeout)
    }

    abstract fun deviceName(): String

    //internal abstract fun datastore(): DataStore<Preferences>
    internal abstract fun files(): Files
    internal abstract fun peers(): Peers
    internal abstract fun storage(): Storage
    internal abstract fun idun(): Idun
    suspend fun shutdown() {
        idun().shutdown()
    }
}


expect fun odin(): Odin

expect fun initializeOdin(context: Context)


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FilesConstructor : RoomDatabaseConstructor<Files> {
    override fun initialize(): Files
}


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
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
        val publicKey = getPublicKey(datastore).first()
        if (privateKey.isNotEmpty() && publicKey.isNotEmpty()) {
            return@runBlocking Keys(PeerId(publicKey), privateKey)
        } else {
            val keys = generateKeys()
            setPrivateKey(datastore, keys.privateKey)
            setPublicKey(datastore, keys.peerId.hash)
            return@runBlocking keys
        }
    }
}
