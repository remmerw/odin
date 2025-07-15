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

        odin.startup()

        odin.publishPeeraddrs(odin.observed, 20, 45)

        val numRelays = odin.numPublifications()
        println("Number of relays $numRelays")
        assertTrue(numRelays > 0)

        odin.shutdown()
    }

}