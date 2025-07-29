package io.github.remmerw.odin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IdunTest {


    @Test
    fun testReservations(): Unit = runBlocking(Dispatchers.IO) {

        val context = org.kmp.testing.context()

        initializeOdin(context)

        val odin = odin()
        assertNotNull(odin)


        val publicAddresses = odin.observedAddresses()

        odin.publishPeeraddrs(publicAddresses, 20, 45)

        val numRelays = odin.numReservations()
        println("Number of relays $numRelays")
        assertTrue(numRelays > 0)

        odin.shutdown()
    }

}