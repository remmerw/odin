package io.github.remmerw.odin.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.remmerw.asen.Peeraddr
import io.github.remmerw.asen.encode58
import io.github.remmerw.odin.generated.resources.Res
import io.github.remmerw.odin.generated.resources.relays_network
import io.github.remmerw.odin.generated.resources.unknown
import io.github.remmerw.odin.odin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource

class StateModel() : ViewModel() {

    var reachability: StringResource by mutableStateOf(Res.string.unknown)

    val numRelays : Flow<Int> = flow {
        while(true) {
            val latestRelays = odin().idun().numReservations()
            emit(latestRelays) // Emits the result of the request to the flow
            delay(500) // Suspends the coroutine for some time
        }
    }

    val numConnections : Flow<Int> = flow {
        while(true) {
            val latestConnections = odin().idun().numIncomingConnections()
            emit(latestConnections) // Emits the result of the request to the flow
            delay(500) // Suspends the coroutine for some time
        }
    }

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


    fun uploadFiles(name: String) {
        odin().uploadFiles(name)
    }

    fun reset() {
        viewModelScope.launch {
            odin().storage().reset()
            odin().files().reset()
            odin().initPage()
        }
    }

    fun fileInfos(): Flow<List<FileInfo>> {
        return odin().files().filesDao().flowFileInfos()
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
        return "pns://" + encode58(odin().idun().peerId().hash)
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
        return runBlocking { odin().idun().incomingConnections() } // todo is this smart ???
    }

    fun reservations(): List<Peeraddr> {
        return runBlocking { odin().idun().reservations() } // todo is this smart ???
    }

    enum class Reachability {
        UNKNOWN, RELAYS
    }

}