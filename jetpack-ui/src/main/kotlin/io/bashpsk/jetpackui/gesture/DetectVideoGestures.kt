package io.bashpsk.jetpackui.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

private const val DEAD_ZONE_PERCENTAGE = 0.05F // 5%

@OptIn(ExperimentalComposeUiApi::class)
internal suspend fun PointerInputScope.detectVideoGestures(
    screenSize: Size = Size.Zero,
    deadZone: Float? = DEAD_ZONE_PERCENTAGE,
    onDragStart: (position: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDragAmount: (
        change: PointerInputChange,
        dragAmount: Offset,
        direction: PointerDirection
    ) -> Unit
) {

    if (screenSize == Size.Zero) return

    val effectiveDeadZone = deadZone ?: DEAD_ZONE_PERCENTAGE

    awaitEachGesture {

        val downEvent = awaitFirstDown(requireUnconsumed = false)
        val pointerId = downEvent.id
        var currentSlop = Offset.Zero

        val viewWidth = screenSize.width
        val viewHeight = screenSize.height

        val deadZoneWidth = viewWidth * effectiveDeadZone
        val deadZoneHeight = viewHeight * effectiveDeadZone

        val topDeadZoneEnd = deadZoneHeight
        val bottomDeadZoneStart = viewHeight - deadZoneHeight
        val leftDeadZoneEnd = deadZoneWidth
        val rightDeadZoneStart = viewWidth - deadZoneWidth

        val centerHorizontalMargin = viewHeight * effectiveDeadZone
        val centerHorizontalStart = (viewHeight - centerHorizontalMargin) / 2F
        val centerHorizontalEnd = centerHorizontalStart + centerHorizontalMargin

        val centerVerticalMargin = viewWidth * effectiveDeadZone
        val centerVerticalStart = (viewWidth - centerVerticalMargin) / 2F
        val centerVerticalEnd = centerVerticalStart + centerVerticalMargin

        val initialPointerChange = awaitPointerSlopOrCancellation(
            pointerId = pointerId,
            pointerType = downEvent.type,
            pointerDirectionConfig = BidirectionalPointerDirectionConfig
        ) { change, slopAmount ->

            change.consume()
            currentSlop = slopAmount
        }

        initialPointerChange?.let { inputChange ->

            val touchX = inputChange.position.x
            val touchY = inputChange.position.y

            val isInTopDeadZone = touchY < topDeadZoneEnd
            val isInBottomDeadZone = touchY >= bottomDeadZoneStart
            val isInLeftDeadZone = touchX < leftDeadZoneEnd
            val isInRightDeadZone = touchX >= rightDeadZoneStart

            val isInCenterHorizontalDeadZone = touchY >= centerHorizontalStart
                    && touchY < centerHorizontalEnd

            val isInCenterVerticalDeadZone = touchX >= centerVerticalStart
                    && touchX < centerVerticalEnd

            val isStartedInDeadZone = isInTopDeadZone || isInBottomDeadZone || isInLeftDeadZone
                    || isInRightDeadZone || isInCenterHorizontalDeadZone
                    || isInCenterVerticalDeadZone

            val initialDirection = when (isStartedInDeadZone) {

                true -> PointerDirection.Unknown

                else -> {

                    val isTopHalf = touchY < viewHeight / 2F
                    val isLeftHalf = touchX < viewWidth / 2F
                    val deltaXFromCenter = touchX - (viewWidth / 2F)
                    val deltaYFromCenter = touchY - (viewHeight / 2F)
                    val isHorizontalDominant = abs(deltaXFromCenter) > abs(deltaYFromCenter)

                    when {

                        isHorizontalDominant && isTopHalf -> PointerDirection.HorizontalTop
                        isHorizontalDominant -> PointerDirection.HorizontalBottom
                        isLeftHalf -> PointerDirection.VerticalLeft
                        else -> PointerDirection.VerticalRight
                    }
                }
            }

            onDragStart(inputChange.position)
            onDragAmount(inputChange, currentSlop, initialDirection)

            when (initialDirection) {

                PointerDirection.Unknown -> {

                    val wasDragSuccessful = drag(pointerId) { change ->

                        onDragAmount(change, change.positionChange(), PointerDirection.Unknown)
                        change.consume()
                    }

                    if (wasDragSuccessful) onDragEnd() else onDragCancel()
                }

                else -> {

                    var currentDirection = initialDirection

                    val wasDragSuccessful = drag(pointerId) { change ->

                        val dragPosition = change.positionChange()
                        val isDragValid = dragPosition.x != 0F || dragPosition.y != 0F

                        (isDragValid).takeIf { isValid -> isValid }?.let { _ ->

                            val isDragHorizontal = abs(dragPosition.x) > abs(dragPosition.y)
                            val isDragVertical = abs(dragPosition.y) > abs(dragPosition.x)

                            val isHorizontal = currentDirection.hasHorizontal()
                            val isVertical = currentDirection.hasVertical()

                            val isLockVerticalDrag = isHorizontal && isDragVertical
                            val isLockHorizontalDrag = isVertical && isDragHorizontal
                            val isDragLocked = isLockVerticalDrag || isLockHorizontalDrag

                            (isDragLocked).takeIf { isLocked -> isLocked }?.let { _ ->

                                val initialTouchIsInLeftHalf = touchX < viewWidth / 2F
                                val initialTouchIsInTopHalf = touchY < viewHeight / 2F

                                currentDirection = when {

                                    isDragVertical -> when {

                                        initialTouchIsInLeftHalf -> PointerDirection.VerticalLeft
                                        else -> PointerDirection.VerticalRight
                                    }

                                    isDragHorizontal -> when {

                                        initialTouchIsInTopHalf -> PointerDirection.HorizontalTop
                                        else -> PointerDirection.HorizontalBottom
                                    }

                                    else -> currentDirection
                                }
                            }
                        }

                        onDragAmount(change, change.positionChange(), currentDirection)
                        change.consume()
                    }

                    if (wasDragSuccessful) onDragEnd() else onDragCancel()
                }
            }
        }
    }
}