package io.bashpsk.jetpackui.pdfview

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.bashpsk.emptyformat.EmptyFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LazyPdfLayout(
    modifier: Modifier = Modifier,
    state: PdfViewState,
    pageSpacing: Dp = 8.dp
) {

    val density = LocalDensity.current
    val pdfLazyListState = rememberLazyListState()

    val bufferPages = 3

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

        state.zoomScale = (state.zoomScale * zoomChange).coerceIn(0.5f, 5f)
        state.position += panChange
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

            CircularProgressIndicator()

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

        val availableWidthPx = with(density) { maxWidth.toPx().toInt() }
        val defaultPageAspectRatio = 1f / 1.414f
        val calculatedPageHeightPx = (availableWidthPx * (1 / defaultPageAspectRatio)).toInt()

        val targetPageSize by remember(availableWidthPx, calculatedPageHeightPx) {
            derivedStateOf {
                IntSize(availableWidthPx.coerceAtLeast(1), calculatedPageHeightPx.coerceAtLeast(1))
            }
        }

        LaunchedEffect(firstVisiblePage, lastVisiblePage, state.totalPages, targetPageSize) {

            if (state.totalPages > 0 && targetPageSize.width > 0 && targetPageSize.height > 0) {

                val startPreload = max(0, firstVisiblePage - bufferPages)
                val endPreload = min(state.totalPages - 1, lastVisiblePage + bufferPages)

                for (page in startPreload..endPreload) {

                    if (state.getCachedBitmap(page) == null) {

                        state.requestPageBitmap(pageIndex = page, targetSize = targetPageSize)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = state.zoomScale,
                    scaleY = state.zoomScale,
                    translationX = state.position.x,
                    translationY = state.position.y
                ),
            state = pdfLazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = pageSpacing)
        ) {

            items(
                count = state.totalPages,
                key = { pageIndex -> "PAGE-$pageIndex" }
            ) { pageIndex ->

                val loadedBitmap by produceState(
                    initialValue = state.getCachedBitmap(pageIndex = pageIndex),
                    key1 = pageIndex,
                    key2 = targetPageSize,
                    key3 = state.totalPages
                ) {

                    var currentBitmap = state.getCachedBitmap(pageIndex = pageIndex)

                    if (currentBitmap == null) {

                        state.requestPageBitmap(pageIndex, targetPageSize)

                        for (attempt in 0..5) {

                            delay(100L * (attempt + 1))
                            currentBitmap = state.getCachedBitmap(pageIndex = pageIndex)
                            if (currentBitmap != null || !isActive) break
                        }
                    }

                    value = currentBitmap
                }

                loadedBitmap?.let { bitmap ->

                    val bitmapAspectRatio by remember(bitmap) {
                        derivedStateOf {
                            EmptyFormat.findAspectRatio(
                                width = bitmap.width,
                                height = bitmap.height
                            )
                        }
                    }

                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(ratio = bitmapAspectRatio)
                            .drawWithContent {

                                drawRect(
                                    color = Color.White,
                                    colorFilter = state.filterType?.colorFilter
                                )

                                drawContent()
                            },
                        bitmap = bitmap.asImageBitmap(),
                        contentScale = ContentScale.Fit,
                        colorFilter = state.filterType?.colorFilter,
                        contentDescription = "PDF Page ${pageIndex + 1}"
                    )
                } ?: run {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(defaultPageAspectRatio),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}