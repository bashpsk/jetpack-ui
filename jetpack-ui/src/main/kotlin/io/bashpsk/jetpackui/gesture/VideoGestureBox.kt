package io.bashpsk.jetpackui.gesture

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun VideoGestureBox(
    modifier: Modifier = Modifier,
    config: VideoGestureConfig = VideoGestureConfig(),
    onTapChanges: (changes: TapChanges) -> Unit = {},
    onDragChanges: (changes: DragChanges) -> Unit = {},
    content: @Composable @UiComposable BoxWithConstraintsScope.() -> Unit
) {

    val dragGestureCoroutineScope = rememberCoroutineScope()

    var screenWidth by rememberSaveable { mutableFloatStateOf(0.0F) }
    var screenHeight by rememberSaveable { mutableFloatStateOf(0.0F) }

    val screenSize by remember(screenWidth, screenHeight) {
        derivedStateOf { Size(width = screenWidth, height = screenHeight) }
    }

    var dragGestureAction by rememberSaveable { mutableStateOf<DragGestureAction?>(null) }
    var touchCount by rememberSaveable { mutableIntStateOf(0) }
    val isOneTouch by remember(touchCount) { derivedStateOf { touchCount == 1 } }
    val isTwoTouch by remember(touchCount) { derivedStateOf { touchCount == 2 } }

    var dragSwipeMinimum by remember { mutableStateOf(Offset.Zero) }

    val screenSizeChanged = Modifier.onSizeChanged { size ->

        screenWidth = size.width.toFloat()
        screenHeight = size.height.toFloat()
    }

    val touchPointerInput = Modifier.pointerInput(screenSize) {

        awaitEachGesture {

            do {

                val event = awaitPointerEvent()

                touchCount = event.changes.size
            } while (event.changes.any { change -> change.pressed })
        }
    }

    val tapPointerInput = Modifier.pointerInput(screenSize) {

        detectTapGestures(
            onTap = { position: Offset ->

                onTapChanges(TapChanges.SingleTap(position))
            },
            onDoubleTap = { position: Offset ->

                val isBackward = position.x in 0.0F..(screenWidth / 2)
                val isForward = position.x in (screenWidth / 2)..screenWidth

                when {

                    config.isDoubleTapEnable && isBackward -> {

                        onTapChanges(TapChanges.BackwardTap(position))
                    }

                    config.isDoubleTapEnable && isForward -> {

                        onTapChanges(TapChanges.ForwardTap(position))
                    }

                    else -> onTapChanges(TapChanges.Unknown)
                }
            }
        )
    }

    val dragPointerInput = Modifier.pointerInput(screenSize) {

        detectVideoGestures(
            screenSize = screenSize,
            onDragStart = { offset: Offset ->

                onDragChanges(DragChanges.DragStart(offset))
            },
            onDragCancel = {

                onDragChanges(DragChanges.DragCanceled)
                dragGestureCoroutineScope.coroutineContext.cancelChildren()

                dragGestureCoroutineScope.launch(context = Dispatchers.Default) {

                    delay(duration = 1000.milliseconds)
                    dragGestureAction = null
                }
            },
            onDragEnd = {

                onDragChanges(DragChanges.DragEnded)
                dragGestureCoroutineScope.coroutineContext.cancelChildren()

                dragGestureCoroutineScope.launch(context = Dispatchers.Default) {

                    delay(duration = 1000.milliseconds)
                    dragGestureAction = null
                }
            }
        ) { change, dragAmount, direction ->

            dragSwipeMinimum += dragAmount

            when {

                isTwoTouch -> {

                    dragGestureAction = when (dragGestureAction) {

                        DragGestureAction.Transform -> dragGestureAction
                        else -> null
                    }

                    change.changedToUp()
                    return@detectVideoGestures
                }
            }

            val isHorizontal = direction == PointerDirection.HORIZONTAL
            val isLeftVertical = direction == PointerDirection.LEFT_VERTICAL
            val isRightVertical = direction == PointerDirection.RIGHT_VERTICAL

            when {

                isHorizontal && config.isHorizontalSeekEnable -> when {

                    abs(x = dragSwipeMinimum.x) > config.minimumLengthSeek -> {

                        when (dragGestureAction) {

                            null -> {

                                dragGestureAction = DragGestureAction.HorizontalSeek
                                change.consume()
                            }

                            DragGestureAction.HorizontalSeek -> {

                                onDragChanges(DragChanges.SeekChanges(dragAmount.x))
                                change.consume()
                            }

                            else -> {}
                        }

                        dragSwipeMinimum = Offset.Zero
                    }
                }

                isLeftVertical && config.isBrightnessEnable -> when {

                    abs(x = dragSwipeMinimum.y) > config.minimumLengthBrightness -> {

                        when (dragGestureAction) {

                            null -> {

                                dragGestureAction = DragGestureAction.Brightness
                                change.consume()
                            }

                            DragGestureAction.Brightness -> {

                                val brightness = when (dragAmount.y > 0.0F) {

                                    true -> ValueChange.Decreased
                                    false -> ValueChange.Increased
                                }

                                onDragChanges(DragChanges.BrightnessChanges(brightness))
                                change.consume()
                            }

                            else -> {}
                        }

                        dragSwipeMinimum = Offset.Zero
                    }
                }

                isRightVertical && config.isVolumeEnable -> when {

                    abs(x = dragSwipeMinimum.y) > config.minimumLengthVolume -> {

                        when (dragGestureAction) {

                            null -> {

                                dragGestureAction = DragGestureAction.Volume
                                change.consume()
                            }

                            DragGestureAction.Volume -> {

                                val volume = when (dragAmount.y > 0.0F) {

                                    true -> ValueChange.Decreased
                                    false -> ValueChange.Increased
                                }

                                onDragChanges(DragChanges.VolumeChanges(volume))
                                change.consume()
                            }

                            else -> {}
                        }

                        dragSwipeMinimum = Offset.Zero
                    }
                }

                else -> {

                    change.changedToUp()
                    onDragChanges(DragChanges.Unknown)
                }
            }
        }
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, rotationChange ->

        if (config.isPanEnable.not() && config.isZoomEnable.not()) return@rememberTransformableState

        when (isTwoTouch) {

            true -> when (dragGestureAction) {

                null -> {

                    if (config.isZoomEnable || config.isPanEnable) {

                        dragGestureAction = DragGestureAction.Transform
                    }
                }

                DragGestureAction.Transform -> {

                    if (config.isZoomEnable) {

                        onDragChanges(DragChanges.ZoomChanges(zoomChange))
                    }

                    if (config.isPanEnable) {

                        onDragChanges(DragChanges.PanChanges(panChange))
                    }

                    dragGestureCoroutineScope.coroutineContext.cancelChildren()

                    dragGestureCoroutineScope.launch(context = Dispatchers.Default) {

                        delay(duration = 1000.milliseconds)
                        dragGestureAction = null
                    }
                }

                else -> return@rememberTransformableState
            }

            false -> {

                when (dragGestureAction) {

                    DragGestureAction.Transform -> null
                    else -> {}
                }

                return@rememberTransformableState
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .then(touchPointerInput)
            .transformable(state = transformableState)
            .then(tapPointerInput)
            .then(dragPointerInput)
            .then(screenSizeChanged),
        contentAlignment = Alignment.Center
    ) {

        content()
    }
}

private enum class DragGestureAction {

    Transform,
    Volume,
    Brightness,
    HorizontalSeek;
}