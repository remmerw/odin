package io.github.remmerw.odin.core

@Suppress("unused")
data class MimeType(val name: String) {
    companion object {
        val PDF_MIME_TYPE: MimeType = MimeType("application/pdf")
        val OCTET_MIME_TYPE: MimeType = MimeType("application/octet-stream")
        val JSON_MIME_TYPE: MimeType = MimeType("application/json")
        val PLAIN_MIME_TYPE: MimeType = MimeType("text/plain")
        val TORRENT_MIME_TYPE: MimeType = MimeType("application/x-bittorrent")
        val CSV_MIME_TYPE: MimeType = MimeType("text/csv")
        val PGP_KEYS_MIME_TYPE: MimeType = MimeType("application/pgp-keys")
        val EXCEL_MIME_TYPE: MimeType = MimeType("application/msexcel")
        val OPEN_EXCEL_MIME_TYPE: MimeType =
            MimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        val WORD_MIME_TYPE: MimeType = MimeType("application/msword")
        val DIR_MIME_TYPE: MimeType = MimeType("vnd.android.document/directory")
        val MHT_MIME_TYPE: MimeType = MimeType("multipart/related")
        val HTML_MIME_TYPE: MimeType = MimeType("text/html")

        // general
        val AUDIO: MimeType = MimeType("audio")
        val VIDEO: MimeType = MimeType("video")
        val TEXT: MimeType = MimeType("text")
        val APPLICATION: MimeType = MimeType("application")
        val IMAGE: MimeType = MimeType("image")
        val ALL: MimeType = MimeType("*/*")
    }
}