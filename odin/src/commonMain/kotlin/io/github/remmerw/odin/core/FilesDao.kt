package io.github.remmerw.odin.core

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FilesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileInfo(fileInfo: FileInfo): Long

    @Delete
    suspend fun delete(fileInfo: FileInfo)

    @Query("SELECT * FROM FileInfo ORDER BY idx DESC")
    fun flowFileInfos(): Flow<List<FileInfo>>

    @Query("SELECT * FROM FileInfo WHERE work IS NULL")
    suspend fun fileInfos(): List<FileInfo>

    @Query("UPDATE FileInfo SET cid =:cid, work = NULL WHERE idx = :idx")
    suspend fun done(idx: Long, cid: Long)

    @Query("DELETE FROM FileInfo WHERE idx = :idx")
    suspend fun delete(idx: Long)

    @Query("SELECT name FROM FileInfo")
    suspend fun names(): List<String>
}
