package io.github.remmerw.odin.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val privateKey = byteArrayPreferencesKey("privateKey")

suspend fun setPrivateKey(dataStore: DataStore<Preferences>, key: ByteArray) {
    dataStore.edit { settings ->
        settings[privateKey] = key
    }
}

fun getPrivateKey(dataStore: DataStore<Preferences>): Flow<ByteArray> =
    dataStore.data.map { settings ->
        settings[privateKey] ?: byteArrayOf()
    }

