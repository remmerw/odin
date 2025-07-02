package io.github.remmerw.odin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
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

        delay(45000)

        val numRelays = odin.idun().numReservations()
        println("Number of relays $numRelays")
        assertTrue(numRelays > 0)

        odin.idun().shutdown()
    }

}