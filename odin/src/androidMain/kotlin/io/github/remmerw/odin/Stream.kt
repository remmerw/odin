package io.github.remmerw.odin

import io.github.remmerw.idun.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import java.io.IOException
import java.io.InputStream
import kotlin.math.min

class Stream(private val channel: Channel) : InputStream() {
    private var buffer: Buffer? = null

    override fun available(): Int {
        return channel.size().toInt()
    }

    // todo maybe not blocking
    private fun loadNextData(): Unit = runBlocking {
        try {
            buffer = channel.next()
        } catch (throwable: Throwable) {
            throw IOException(throwable)
        }
    }

    private fun hasData(): Boolean {
        if (buffer == null) { // initial the buffer is null
            loadNextData()
        }
        if (buffer == null) {
            return false
        }
        if (buffer!!.exhausted()) {
            return false
        }
        return true
    }

    override fun read(bytes: ByteArray, off: Int, len: Int): Int {
        if (!hasData()) return -1

        val max = min(len, buffer!!.size.toInt())
        val read = buffer!!.readAtMostTo(bytes, off, off + max)

        if (buffer!!.exhausted()) {
            loadNextData()
        }

        return read
    }


    override fun read(bytes: ByteArray): Int {
        return read(bytes, 0, bytes.size)
    }


    override fun read(): Int {
        if (!hasData()) return -1
        if (!buffer!!.exhausted()) {
            return buffer!!.readByte().toInt() and 0xFF
        } else {
            loadNextData()
            return read()
        }
    }


    override fun skip(n: Long): Long {
        channel.seek(n)
        loadNextData()
        return n
    }
}