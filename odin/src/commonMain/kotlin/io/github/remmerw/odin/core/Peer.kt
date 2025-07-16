package io.github.remmerw.odin.core

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.remmerw.asen.PeerId
import io.github.remmerw.asen.Peeraddr

@Entity
data class Peer(
    @field:PrimaryKey val peerId: PeerId,
    @field:ColumnInfo val address: ByteArray,
    @field:ColumnInfo val port: Int
) {
    fun peeraddr(): Peeraddr {
        return Peeraddr(peerId, address, port.toUShort())
    }

    override fun hashCode(): Int {
        return peerId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Peer

        if (peerId != other.peerId) return false
        if (!address.contentEquals(other.address)) return false
        if (port != other.port) return false

        return true
    }
}
