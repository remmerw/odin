package io.github.remmerw.odin.core

import io.github.remmerw.asen.PeerId
import io.github.remmerw.idun.pnsUri
import kotlin.time.ExperimentalTime


const val META: String =
    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=2, user-scalable=yes\">" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
val STYLE: String = """
            <style>
                  body {
                    background-color: white;
                    color: black;
                    font-size: 18px;
                  }
                  h2 {
                    text-align: center;
                  }
                  h3 {
                    text-align: center;
                  }
                  h4 {
                    text-align: center;
                  }
                 .footer {
                    position: fixed;
                    left: 0;
                    bottom: 0;
                    width: 100%;
                    background-color: white;
                    color: black;
                    font-size: 14px;
                    text-align: center;
                 }
             
             @media (prefers-color-scheme: dark) {
                  body {
                    background-color: #222222;
                    color: white;
                    font-size: 18px;
                  }
                 .footer {
                    position: fixed;
                    left: 0;
                    bottom: 0;
                    width: 100%;
                    background-color: #222222;
                    color: white;
                    font-size: 14px;
                    text-align: center;
                 }
                 /* unvisited link */
                 a:link {
                    color: #0F82AF;
                 }
                 
                 /* visited link */
                 a:visited {
                    color: green;
                 }
                 
                 /* mouse over link */
                 a:hover {
                    color: inherit;
                 }
                 
                 /* selected link */
                 a:active {
                    color: inherit;
                 } }

            @media (prefers-color-scheme: light) {
                  body {
                    background-color: white;
                    color: black;
                    font-size: 18px;
                  }
                 .footer {
                    position: fixed;
                    left: 0;
                    bottom: 0;
                    width: 100%;
                    background-color: white;
                    color: black;
                    font-size: 14px;
                    text-align: center;
                 }
            }</style>
            """.trimIndent()

private const val SVG_FOLDER =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"  version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\" d=\"M10,4H4C2.89,4 2,4.89 2,6V18C2,19.1 2.9,20 4,20H20C21.1,20 22,19.1 22,18V8C22,6.89 21.1,6 20,6H12L10,4Z\" /></svg>"

private const val SVG_DOWNLOAD: String =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\" d=\"M5,20H19V18H5M19,9H15V3H9V9H5L12,16L19,9Z\" /></svg>"
private const val SVG_OCTET =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"  version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\" d=\"M14,2H6C4.89,2 4,2.9 4,4V20C4,21.1 4.9,22 6,22H18C19.1,22 20,21.1 20,20V8L14,2M14.5,18.9L12,17.5L9.5,19L10.2,16.2L8,14.3L10.9,14.1L12,11.4L13.1,14L16,14.2L13.8,16.1L14.5,18.9M13,9V3.5L18.5,9H13Z\" /></svg>"
private const val SVG_TEXT =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\" d=\"M6,2C4.9,2 4,2.9 4,4V20C4,21.1 4.9,22 6,22H18C19.1,22 20,21.1 20,20V8L14,2H6M6,4H13V9H18V20H6V4M8,12V14H16V12H8M8,16V18H13V16H8Z\" /></svg>"
private const val SVG_APPLICATION =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\" d=\"M19,4C20.11,4 21,4.9 21,6V18C21,19.1 20.1,20 19,20H5C3.89,20 3,19.1 3,18V6C3,4.9 3.9,4 5,4H19M19,18V8H5V18H19Z\" /></svg>"
private const val SVG_PDF =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\" d=\"M12,10.5H13V13.5H12V10.5M7,11.5H8V10.5H7V11.5M20,6V18C20,19.1 19.1,20 18,20H6C4.9,20 4,19.1 4,18V6C4,4.9 4.9,4 6,4H18C19.1,4 20,4.9 20,6M9.5,10.5C9.5,9.67 8.83,9 8,9H5.5V15H7V13H8C8.83,13 9.5,12.33 9.5,11.5V10.5M14.5,10.5C14.5,9.67 13.83,9 13,9H10.5V15H13C13.83,15 14.5,14.33 14.5,13.5V10.5M18.5,9H15.5V15H17V13H18.5V11.5H17V10.5H18.5V9Z\" /></svg>"
private const val SVG_MOVIE =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\" d=\"M5.76,10H20V18H4V6.47M22,4H18L20,8H17L15,4H13L15,8H12L10,4H8L10,8H7L5,4H4C2.9,4 2,4.9 2,6V18C2,19.1 2.9,20 4,20H20C21.1,20 22,19.1 22,18V4Z\" /></svg>"
private const val SVG_IMAGE =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\"  d=\"M12,10L11.06,12.06L9,13L11.06,13.94L12,16L12.94,13.94L15,13L12.94,12.06L12,10M20,5H16.83L15,3H9L7.17,5H4A2,2 0 0,0 2,7V19A2,2 0 0,0 4,21H20A2,2 0 0,0 22,19V7A2,2 0 0,0 20,5M20,19H4V7H8.05L8.64,6.35L9.88,5H14.12L15.36,6.35L15.95,7H20V19M12,8A5,5 0 0,0 7,13A5,5 0 0,0 12,18A5,5 0 0,0 17,13A5,5 0 0,0 12,8M12,16A3,3 0 0,1 9,13A3,3 0 0,1 12,10A3,3 0 0,1 15,13A3,3 0 0,1 12,16Z\" /></svg>"
private const val SVG_AUDIO =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\"  d=\"M14,3.23V5.29C16.89,6.15 19,8.83 19,12C19,15.17 16.89,17.84 14,18.7V20.77C18,19.86 21,16.28 21,12C21,7.72 18,4.14 14,3.23M16.5,12C16.5,10.23 15.5,8.71 14,7.97V16C15.5,15.29 16.5,13.76 16.5,12M3,9V15H7L12,20V4L7,9H3Z\" /></svg>"


private fun svgResource(mimeType: String): String {
    if (mimeType.isNotEmpty()) {
        if (mimeType == MimeType.DIR_MIME_TYPE.name) {
            return SVG_FOLDER
        }
        if (mimeType == MimeType.OCTET_MIME_TYPE.name) {
            return SVG_OCTET
        }
        if (mimeType == MimeType.PDF_MIME_TYPE.name) {
            return SVG_PDF
        }
        if (mimeType.startsWith(MimeType.TEXT.name)) {
            return SVG_TEXT
        }
        if (mimeType.startsWith(MimeType.VIDEO.name)) {
            return SVG_MOVIE
        }
        if (mimeType.startsWith(MimeType.IMAGE.name)) {
            return SVG_IMAGE
        }
        if (mimeType.startsWith(MimeType.AUDIO.name)) {
            return SVG_AUDIO
        }
        if (mimeType.startsWith(MimeType.APPLICATION.name)) {
            return SVG_APPLICATION
        }
    }
    return SVG_OCTET
}

private fun fileSize(size: Long): String {
    val fileSize: String

    if (size < 1000) {
        fileSize = size.toString()
        return "$fileSize B"
    } else if (size < 1000 * 1000) {
        fileSize = (size / 1000).toDouble().toString()
        return "$fileSize KB"
    } else {
        fileSize = (size / (1000 * 1000)).toDouble().toString()
        return "$fileSize MB"
    }
}

const val CONTENT_DOWNLOAD: String = "Content-Download"



@OptIn(ExperimentalTime::class)
fun directoryContent(peerId: PeerId, links: List<FileInfo>, title: String): String {

    val answer = StringBuilder(
        "<html>" + "<head>" + META +
                "<title>" + title + "</title>"
    )
    answer.append("</head>")
    answer.append(STYLE)
    answer.append("<body>")
    answer.append("<h3>")
    answer.append(title)
    answer.append("</h3>")

    if (links.isNotEmpty()) {
        answer.append("<form><table  width=\"100%\" style=\"border-spacing: 8px;\">")


        for (info in links) {
            val mimeType = info.mimeType

            answer.append("<tr>")

            answer.append("<td>")
            answer.append(svgResource(mimeType))
            answer.append("</td>")

            answer.append("<td width=\"100%\" style=\"word-break:break-word\">")
            answer.append("<a href=\"")
            answer.append(pnsUri(peerId, info.cid))
            answer.append("\">")
            answer.append(info.name)
            answer.append("</a>")
            answer.append("</td>")

            answer.append("<td>")
            answer.append(fileSize(info.size))
            answer.append("</td>")

            answer.append("<td align=\"center\">")

            val name = info.name

            val text = "<button style=\"float:none!important;display:inline;\" " +
                    "name=\"" + CONTENT_DOWNLOAD + "\" " +
                    "value=\"" + name + "\" " +
                    "formenctype=\"text/plain\" " +
                    "formmethod=\"get\" " +
                    "type=\"submit\" " +
                    "formaction=\"" +
                    pnsUri(peerId, info.cid) + "\">" + SVG_DOWNLOAD + "</button>"

            answer.append(text)
            answer.append("</td>")
            answer.append("</tr>")
        }
        answer.append("</table></form>")
    }

    return answer.toString()
}