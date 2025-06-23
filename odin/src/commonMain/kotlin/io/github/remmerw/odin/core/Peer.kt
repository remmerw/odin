package io.github.remmerw.odin.core

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.remmerw.asen.PeerId
import io.github.remmerw.asen.Peeraddr
import io.github.remmerw.asen.parsePeeraddr

@Entity
data class Peer(
    @field:ColumnInfo(name = "peerId") @field:PrimaryKey val peerId: PeerId,
    @field:ColumnInfo(typeAffinity = ColumnInfo.BLOB) val raw: ByteArray
) {
    fun peeraddr(): Peeraddr {
        return parsePeeraddr(peerId, raw)
    }


    override fun hashCode(): Int {
        var result = peerId.hashCode()
        result = 31 * result + raw.contentHashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Peer

        if (peerId != other.peerId) return false
        if (!raw.contentEquals(other.raw)) return false

        return true
    }
}
