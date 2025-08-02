package io.bashpsk.jetpackui.pdfview

import android.net.Uri

sealed interface PdfSource {

    data class FromPath(val path: String) : PdfSource

    data class FromUri(val uri: Uri) : PdfSource
}