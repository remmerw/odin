package io.github.remmerw.odin.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.remmerw.asen.Peeraddr
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.relays_network
import io.github.remmerw.odin.generated.resources.unknown
import io.github.remmerw.odin.odin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class StateModel() : ViewModel() {

    var reachability: StringResource by mutableStateOf(Res.string.unknown)


    fun homepageImplemented(): Boolean {
        return odin().homepageImplemented()
    }

    fun reachability(reachability: Reachability) {
        this.reachability = networkReachability(reachability)
    }

    fun networkReachability(reachability: Reachability): StringResource {
        return when (reachability) {
            Reachability.UNKNOWN -> Res.string.unknown
            Reachability.RELAYS -> Res.string.relays_network
        }
    }

    fun cancelWork(fileInfo: FileInfo) {
        odin().cancelWork(fileInfo)
    }

    fun numRelays(): Int {
        return odin().numRelays
    }

    fun uploadFiles(name: String) {
        odin().uploadFiles(name)
    }

    fun reset() {
        viewModelScope.async(Dispatchers.IO) {
            odin().storage().reset()
            odin().files().clearAllTables() // should not run on Main UI thread
            odin().initPage()
        }
    }

    fun numConnections(): Int {
        return odin().numConnections
    }

    fun fileInfos(): Flow<List<FileInfo>> {
        return odin().files().filesDao().flowFileInfos()
    }

    fun reserveActive(): Boolean {
        return odin().reserveActive
    }


    fun makeReservations() {
        viewModelScope.launch {
            odin().makeReservations()
        }
    }

    suspend fun sharePageUri(uri: String) {
        odin().sharePageUri(uri)
    }

    fun pageUri(): String {
        return "pns:://" + odin().idun().peerId().toBase58()
    }


    fun delete(fileInfo: FileInfo) {

        viewModelScope.launch {
            // Note: the content itself (in the block store) will not be deleted
            // this is a limitation (idea the user has to cleanup the block store
            // from time to time
            odin().files().filesDao().delete(fileInfo)

            odin().initPage()

        }

    }

    fun evaluateReachability(): Reachability {
        return if (!odin().reservationFeaturePossible()) {
            Reachability.UNKNOWN
        } else {
            Reachability.RELAYS
        }
    }


    fun incomingConnections(): List<String> {
        return odin().idun().incomingConnections()
    }

    fun reservations(): List<Peeraddr> {
        return odin().idun().reservations()
    }

    enum class Reachability {
        UNKNOWN, RELAYS
    }

}