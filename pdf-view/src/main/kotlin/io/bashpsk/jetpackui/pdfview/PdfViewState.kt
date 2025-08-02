package io.bashpsk.jetpackui.pdfview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import io.bashpsk.imagekolor.filter.ImageFilterType
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException
import kotlin.coroutines.coroutineContext

@Composable
fun rememberPdfViewState(
    source: PdfSource,
    coroutineScope: CoroutineScope = rememberCoroutineScope { Dispatchers.Default }
): PdfViewState {

    val context = LocalContext.current

    return remember(context, source, coroutineScope) {
        PdfViewState(context = context, source = source, coroutineScope = coroutineScope)
    }
}

@Stable
class PdfViewState(
    private val context: Context,
    internal val source: PdfSource,
    private val coroutineScope: CoroutineScope
) {

    private var pdfRenderer: PdfRenderer? = null

    private var parcelFileDescriptor: ParcelFileDescriptor? = null

    private val bitmapCache = LruCache<Int, Bitmap>(10)
    private val renderMutex = Mutex()

    private val renderJobs = persistentMapOf<Int, Job>()

    var totalPages by mutableIntStateOf(0)
        private set

    var filterType by mutableStateOf<ImageFilterType?>(null)
        private set

    internal var zoomScale by mutableFloatStateOf(1f)
    internal var position by mutableStateOf(Offset.Zero)

    init {

        loadPdf()
    }

    private fun loadPdf() {

        if (renderJobs[-1]?.isActive == true) return

        val newJob = coroutineScope.launch {

            try {

                parcelFileDescriptor = when (source) {

                    is PdfSource.FromPath -> ParcelFileDescriptor.open(
                        File(source.path),
                        ParcelFileDescriptor.MODE_READ_ONLY
                    )

                    is PdfSource.FromUri -> context.contentResolver.openFileDescriptor(
                        source.uri,
                        "r"
                    )

                    else -> {

                        totalPages = 0
                        return@launch
                    }
                }

                parcelFileDescriptor?.let { descriptor ->

                    pdfRenderer = PdfRenderer(descriptor)
                    totalPages = pdfRenderer?.pageCount ?: 0
                } ?: run {

                    totalPages = 0
                }
            } catch (exception: IOException) {

                Log.e(LOG_TAG, "Error loading PDF : ${exception.message}", exception)
                totalPages = 0
            } catch (exception: SecurityException) {

                Log.e(LOG_TAG, "Error loading PDF : ${exception.message}", exception)
                totalPages = 0
            } finally {

                renderJobs.remove(key = -1)
            }
        }

        renderJobs.put(key = -1, value = newJob)
    }

    fun requestPageBitmap(pageIndex: Int, targetSize: IntSize): Bitmap? {

        if (pageIndex < 0 || pageIndex >= totalPages) return null

        bitmapCache[pageIndex]?.let { bitmap -> return bitmap }

        if (renderJobs[pageIndex]?.isActive == true) return null

        val newJob = coroutineScope.launch(context = SupervisorJob()) {

            try {

                if (isActive) renderPageBitmap(
                    pageIndex = pageIndex,
                    targetSize = targetSize
                )?.let { bitmap ->

                    bitmapCache.put(key = pageIndex, value = bitmap)
                }
            } finally {

                renderJobs.remove(key = pageIndex)
            }
        }

        renderJobs.put(key = pageIndex, value = newJob)

        return null
    }

    private suspend fun renderPageBitmap(pageIndex: Int, targetSize: IntSize): Bitmap? {

        renderMutex.withLock {

            bitmapCache[pageIndex]?.let { bitmap -> return bitmap }

            if (pdfRenderer == null || !coroutineContext.isActive) return null

            return try {

                pdfRenderer?.openPage(pageIndex)?.use { currentPage ->

                    val bitmap = createBitmap(
                        width = targetSize.width.coerceAtLeast(1),
                        height = targetSize.height.coerceAtLeast(1)
                    )

                    currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            } catch (exception: Exception) {

                Log.e(LOG_TAG, "Page Render Error : ${exception.message}", exception)
                null
            }
        }
    }

    fun getCachedBitmap(pageIndex: Int): Bitmap? {

        return bitmapCache[pageIndex]
    }

    fun closeAll() {

        coroutineScope.launch {

            renderMutex.withLock {

                renderJobs.values.forEach { job -> job.cancel() }
                renderJobs.clear()
                bitmapCache.evictAll()

                try {

                    pdfRenderer?.close()
                    parcelFileDescriptor?.close()
                } catch (exception: IOException) {

                    Log.e(LOG_TAG, "Pdf File Close Error : ${exception.message}", exception)
                }

                pdfRenderer = null
                parcelFileDescriptor = null
            }
        }
    }

    fun updateFilterType(filter: ImageFilterType?) {

        filterType = filter
    }
}