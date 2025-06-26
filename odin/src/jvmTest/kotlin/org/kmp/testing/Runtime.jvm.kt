package org.kmp.testing

import io.github.remmerw.odin.Context
import io.github.remmerw.odin.JvmContext

actual fun context() : Context {
    return JvmContext
}