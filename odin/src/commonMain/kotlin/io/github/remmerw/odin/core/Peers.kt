package io.github.remmerw.odin.core

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import io.github.remmerw.asen.PeerId
import io.github.remmerw.asen.PeerStore
import io.github.remmerw.asen.Peeraddr
import io.github.remmerw.odin.PeersConstructor
import kotlinx.coroutines.flow.first

@Database(entities = [Peer::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
@ConstructedBy(PeersConstructor::class)
abstract class Peers : RoomDatabase(), PeerStore {
    abstract fun bootstrapDao(): PeerDao

    override suspend fun peeraddrs(limit: Int): List<Peeraddr> {
        return bootstrapDao().randomPeers(limit).first().map { peer -> peer.peeraddr() }
    }

    override suspend fun store(peeraddr: Peeraddr) {
        bootstrapDao().insertPeer(Peer(peeraddr.peerId, peeraddr.encoded()))
    }
}


object Converters {

    @TypeConverter
    fun toPeerId(data: ByteArray): PeerId {
        return PeerId(data)
    }

    @TypeConverter
    fun toArray(peerId: PeerId): ByteArray {
        return peerId.hash
    }
}