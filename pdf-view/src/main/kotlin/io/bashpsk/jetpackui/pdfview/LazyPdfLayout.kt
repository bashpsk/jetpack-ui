package io.bashpsk.jetpackui.pdfview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LazyPdfLayout(
    modifier: Modifier = Modifier,
    state: PdfViewState,
    pageSpacing: Dp = 4.dp,
    bufferPages: Int = 4,
    zoomRange: ClosedFloatingPointRange<Float> = 0.6F..6F,
) {

    val density = LocalDensity.current
    val pdfLazyListState = rememberLazyListState()

    val visibleItemsInfo by remember(pdfLazyListState) {
        derivedStateOf { pdfLazyListState.layoutInfo.visibleItemsInfo }
    }

    val firstVisiblePage by remember(visibleItemsInfo) {
        derivedStateOf { visibleItemsInfo.firstOrNull()?.index ?: 0 }
    }

    val lastVisiblePage by remember(visibleItemsInfo, state) {
        derivedStateOf {
            visibleItemsInfo.lastOrNull()?.index ?: (state.totalPages - 1).coerceAtLeast(0)
        }
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->

        val newZoomScale = (state.zoomScale * zoomChange).coerceIn(range = zoomRange)

        state.zoomScale = newZoomScale
        if (newZoomScale <= 1f) state.position = Offset.Zero else state.position += panChange
    }

    DisposableEffect(state.source) {

        onDispose {

            state.closeAll()
        }
    }

    if (state.totalPages == 0 && state.getCachedBitmap(pageIndex = -1) == null) {

        BoxWithConstraints(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Loading PDF..."
            )
        }

        return
    }

    if (state.totalPages == 0) {

        BoxWithConstraints(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "PDF is empty or failed to load."
            )
        }

        return
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(shape = RectangleShape)
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center
    ) {

        val pageWidth = with(density) { maxWidth.toPx().toInt().coerceAtLeast(1) }
        val pageHeight = (pageWidth / DefaultPageAspectRatio).toInt().coerceAtLeast(1)

        val pageSize by remember(pageWidth, pageHeight) {
            derivedStateOf { IntSize(width = pageWidth, height = pageHeight) }
        }

        LaunchedEffect(firstVisiblePage, lastVisiblePage, state.totalPages, pageSize) {

            if (state.totalPages > 0 && pageSize != IntSize.Zero) {

                val startPreload = max(0, firstVisiblePage - bufferPages)
                val endPreload = min(state.totalPages - 1, lastVisiblePage + bufferPages)

                for (page in startPreload..endPreload) {

                    if (state.getCachedBitmap(pageIndex = page) == null) {

                        state.requestPageBitmap(pageIndex = page, pageSize = pageSize)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.matchParentSize(),
            state = pdfLazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = pageSpacing)
        ) {

            items(
                count = state.totalPages,
                key = { pageIndex -> "PAGE-$pageIndex" }
            ) { pageIndex ->

                PageContentView(
                    modifier = Modifier.fillParentMaxWidth(),
                    state = state,
                    pageIndex = pageIndex,
                    pageSize = pageSize
                )
            }
        }
    }
}

@Composable
private fun PageContentView(
    modifier: Modifier = Modifier,
    state: PdfViewState,
    pageIndex: Int,
    pageSize: IntSize
) {

    val loadedBitmap by produceState(
        initialValue = state.getCachedBitmap(pageIndex = pageIndex),
        pageIndex,
        state.totalPages
    ) {

        value = state.getCachedBitmap(pageIndex = pageIndex).takeIf { bitmap ->

            bitmap != null
        } ?: run {

            state.requestPageBitmap(pageIndex = pageIndex, pageSize = pageSize)

            var retrievedBitmap: Bitmap? = null

            for (attempt in 0..5) {

                retrievedBitmap = state.getCachedBitmap(pageIndex = pageIndex)
                if (retrievedBitmap != null || !isActive) break
                delay(100L * (attempt + 1))
            }

            retrievedBitmap
        }
    }

    val graphicsLayerModifier = Modifier.graphicsLayer(
        scaleX = state.zoomScale,
        scaleY = state.zoomScale,
        translationX = state.position.x,
        translationY = state.position.y
    )

    val pageBackgroundModifier = Modifier.drawWithContent {

        drawRect(color = Color.White, colorFilter = state.filterType?.colorFilter)

        drawContent()
    }

    loadedBitmap?.let { bitmap ->

        Image(
            modifier = modifier
                .then(graphicsLayerModifier)
                .then(pageBackgroundModifier)
                .clip(shape = MaterialTheme.shapes.extraSmall),
            bitmap = bitmap.asImageBitmap(),
            contentScale = ContentScale.Fit,
            colorFilter = state.filterType?.colorFilter,
            contentDescription = "PDF Page ${pageIndex + 1}"
        )
    } ?: run {

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {

            CircularProgressIndicator()
        }
    }
}

private const val DefaultPageAspectRatio = 1f / 1.414f