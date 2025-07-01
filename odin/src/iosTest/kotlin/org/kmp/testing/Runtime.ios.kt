package org.kmp.testing

import io.github.remmerw.odin.Context
import io.github.remmerw.odin.IosContext

actual fun context(): Context {
    return IosContext
}