package io.github.remmerw.odin.core

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FileInfo(
    @field:PrimaryKey(autoGenerate = true) val idx: Long,
    val name: String,
    val mimeType: String,
    val cid: Long,
    val work: String?,
    val size: Long
)
