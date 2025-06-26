package org.kmp.testing

import io.github.remmerw.odin.Context
import androidx.test.core.app.ApplicationProvider

actual fun context() : Context {
    return ApplicationProvider.getApplicationContext()
}