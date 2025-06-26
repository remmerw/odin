package io.github.remmerw.odin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class IdunTest {


    @Test
    fun test(): Unit = runBlocking(Dispatchers.IO) {

        val context = org.kmp.testing.context()

        initializeOdin(context)

        val odin = odin()
        checkNotNull(odin)

        odin.runService()
        // TODO make real test
        assertTrue(true)
    }
}