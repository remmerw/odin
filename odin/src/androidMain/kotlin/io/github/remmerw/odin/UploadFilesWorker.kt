package io.github.remmerw.odin

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.remmerw.idun.Node
import io.github.remmerw.odin.core.FileInfo
import io.github.remmerw.odin.core.MimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class UploadFilesWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {


    private fun mimeType(context: Context, uri: Uri): String {
        var mimeType = context.contentResolver.getType(uri)
        if (mimeType == null) {
            mimeType = MimeType.Companion.OCTET_MIME_TYPE.name
        }
        return mimeType
    }


    private fun getMimeType(name: String): String {
        val mimeType = evaluateMimeType(name)
        if (mimeType != null) {
            return mimeType
        }
        return MimeType.Companion.OCTET_MIME_TYPE.name
    }

    private fun evaluateMimeType(filename: String): String? {

        val extension = fileExtension(filename)
        if (!extension.isEmpty()) {
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mimeType != null) {
                return mimeType
            }
        }

        return null
    }

    private fun nameWithoutExtension(file: String): String {
        val fileName = File(file).name
        val dotIndex = fileName.lastIndexOf('.')
        return if ((dotIndex == -1)) fileName else fileName.substring(0, dotIndex)
    }

    private fun fileName(context: Context, uri: Uri): String {
        var filename: String? = null

        val contentResolver = context.contentResolver

        contentResolver.query(
            uri,
            null, null, null, null
        ).use { cursor ->
            cursor!!.moveToFirst()
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            filename = cursor.getString(nameIndex)
        }


        if (filename == null) {
            filename = uri.lastPathSegment
        }

        if (filename == null) {
            filename = "file_name_not_detected"
        }

        return filename
    }

    private fun fileSize(context: Context, uri: Uri): Long {
        val contentResolver = context.contentResolver

        contentResolver.openFileDescriptor(uri, "r").use { fd ->
            return fd!!.statSize
        }
    }


    private fun getUniqueName(names: List<String>, name: String): String {
        return getName(names, name, 0)
    }


    private fun fileExtension(fullName: String): String {
        val dotIndex = fullName.lastIndexOf('.')
        return if ((dotIndex == -1)) "" else fullName.substring(dotIndex + 1)
    }


    private fun getName(names: List<String>, name: String, orgIndex: Int): String {
        var index = orgIndex
        var searchName = name
        if (index > 0) {
            try {
                val base = nameWithoutExtension(name)
                val extension = fileExtension(name)
                if (extension.isEmpty()) {
                    searchName = "$searchName ($index)"
                } else {
                    val end = " ($index)"
                    if (base.endsWith(end)) {
                        val realBase = base.substring(0, base.length - end.length)
                        searchName = "$realBase ($index).$extension"
                    } else {
                        searchName = "$base ($index).$extension"
                    }
                }
            } catch (_: Throwable) {
                searchName = "$searchName ($index)" // just backup
            }
        }

        if (names.contains(searchName)) {
            return getName(names, name, ++index)
        }
        return searchName
    }

    private fun checkMimeType(mimeTypeOrg: String?, name: String): String? {
        var mimeType = mimeTypeOrg
        var evalDisplayName = false
        if (mimeType == null) {
            evalDisplayName = true
        } else {
            if (mimeType.isEmpty()) {
                evalDisplayName = true
            } else {
                if (mimeType == MimeType.Companion.OCTET_MIME_TYPE.name) {
                    evalDisplayName = true
                }
            }
        }
        if (evalDisplayName) {
            mimeType = getMimeType(name)
        }
        return mimeType
    }

    override suspend fun doWork(): Result {
        val filename = inputData.getString(FILE)
        checkNotNull(filename)
        val odin = odin()
        val storage = odin.storage()
        val file = File(filename)

        val uris: MutableList<String> = ArrayList()

        try {
            withContext(Dispatchers.IO) {
                BufferedReader(
                    InputStreamReader(FileInputStream(file))
                ).use { reader ->
                    while (reader.ready()) {
                        uris.add(reader.readLine())
                    }
                }

                val names = odin.files().fileNames().toMutableList()

                for (uriStr in uris) {
                    val uri = uriStr.toUri()

                    val displayName = fileName(applicationContext, uri)
                    val uriType = mimeType(applicationContext, uri)
                    val size = fileSize(applicationContext, uri)

                    val mimeType = checkMimeType(uriType, displayName)


                    val name = getUniqueName(names, displayName)
                    val fileInfo = FileInfo(
                        0, name, mimeType!!,
                        0, id.toString(), size
                    )
                    val idx = odin.files().storeFileInfo(fileInfo)

                    try {
                        var node: Node? = null

                        applicationContext.contentResolver
                            .openInputStream(uri).use { inputStream ->
                                checkNotNull(inputStream)
                                val bytes = ByteArray(Short.MAX_VALUE.toInt())
                                object : RawSource {

                                    override fun readAtMostTo(
                                        sink: Buffer,
                                        byteCount: Long
                                    ): Long {
                                        ensureActive()

                                        val read = inputStream.read(bytes)
                                        if (read >= 0) {
                                            sink.write(bytes, 0, read)
                                        }
                                        return read.toLong()

                                    }

                                    override fun close() {
                                        inputStream.close()
                                    }

                                }.buffered().use { source ->
                                    node = storage.storeSource(source, name, mimeType)
                                }

                            }

                        odin.files().done(idx, node!!.cid())
                    } catch (_: Throwable) {
                        odin().files().delete(idx)
                        break
                    } finally {
                        names.add(name) // just for safety
                    }
                }
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            return Result.failure()
        } finally {
            odin().initPage()
            file.deleteOnExit()
        }


        return Result.success()
    }


    companion object {
        private val TAG: String = UploadFilesWorker::class.java.simpleName
        private const val FILE = "file"

        private fun getWork(file: String): OneTimeWorkRequest {
            val data: Data.Builder = Data.Builder()
            data.putString(FILE, file)

            return OneTimeWorkRequestBuilder<UploadFilesWorker>()
                .addTag(TAG)
                .setInputData(data.build())
                .build()
        }

        fun load(context: Context, file: String) {
            WorkManager.Companion.getInstance(context).enqueue(getWork(file))
        }
    }
}