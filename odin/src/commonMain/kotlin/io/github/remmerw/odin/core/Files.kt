package io.github.remmerw.odin.core

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.remmerw.odin.FilesConstructor

@ConstructedBy(FilesConstructor::class)
@Database(entities = [FileInfo::class], version = 1, exportSchema = false)
abstract class Files : RoomDatabase() {
    abstract fun filesDao(): FilesDao

    suspend fun fileInfos(): List<FileInfo> {
        return filesDao().fileInfos()
    }

    suspend fun fileNames(): List<String> {
        return filesDao().names()
    }

    suspend fun storeFileInfo(fileInfo: FileInfo): Long {
        return filesDao().insertFileInfo(fileInfo)
    }

    suspend fun done(idx: Long, cid: Long) {
        filesDao().done(idx, cid)
    }

    suspend fun delete(idx: Long) {
        filesDao().delete(idx)
    }

    suspend fun reset() {
        filesDao().reset()
    }

}


