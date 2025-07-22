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

@OptIn(ExperimentalComposeUiApi::class)
internal suspend fun PointerInputScope.detectVideoGestures(
    screenSize: Size = Size.Zero,
    onDragStart: (position: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDragAmount: (
        change: PointerInputChange,
        dragAmount: Offset,
        direction: PointerDirection
    ) -> Unit
) {

    awaitEachGesture {

        val down = awaitFirstDown(requireUnconsumed = false)
        val event = awaitPointerEvent()

        var pointerInputChange: PointerInputChange?
        var pointerDirection = PointerDirection.HORIZONTAL_AND_VERTICAL
        var overSlop = Offset.Zero

        val screenWidth = screenSize.width
        val screenHeight = screenSize.height

        do {

            pointerInputChange = when {

                event.changes.size == 1 -> awaitPointerSlopOrCancellation(
                    pointerId = down.id,
                    pointerType = down.type,
                    pointerDirectionConfig = BidirectionalPointerDirectionConfig
                ) { change, over ->

                    change.consume()
                    overSlop = over
                }

                else -> null
            }
        } while (pointerInputChange != null && pointerInputChange.isConsumed.not())

        pointerInputChange?.let { inputChange ->

            val pointX = inputChange.position.x
            val pointY = inputChange.position.y
            val verticalSize = screenWidth / 4
            val horizontalSize = screenHeight
//            val horizontalSize = screenHeight / 2

            val isLeftVertical = pointX in (0F * verticalSize)..(1F * verticalSize)
            val isRightVertical = pointX in (3F * verticalSize)..(4F * verticalSize)
            val isBottom = pointY in 0.0F..horizontalSize
//            val isBottom = pointY in (1.0F * horizontalSize)..(2.0F * horizontalSize)

            onDragStart(inputChange.position)
            onDragAmount(inputChange, overSlop, pointerDirection)

            when {

                drag(inputChange.id) { change: PointerInputChange ->

                    val firstPosition = change.historical.firstOrNull()?.position ?: Offset.Zero
                    val secondPosition = change.historical.lastOrNull()?.position ?: Offset.Zero

                    val deltaX = firstPosition.x - secondPosition.x
                    val deltaY = firstPosition.y - secondPosition.y

                    val isHorizontal = abs(deltaX) > abs(deltaY)
                    val isVertical = abs(deltaX) < abs(deltaY)

                    val isBoth = pointerDirection == PointerDirection.HORIZONTAL_AND_VERTICAL
                    val isHorizontalDirection = pointerDirection == PointerDirection.HORIZONTAL
                    val isLeftDirection = pointerDirection == PointerDirection.LEFT_VERTICAL
                    val isRightDirection = pointerDirection == PointerDirection.RIGHT_VERTICAL

                    val direction = when {

                        isHorizontalDirection -> PointerDirection.HORIZONTAL
                        isLeftDirection -> PointerDirection.LEFT_VERTICAL
                        isRightDirection -> PointerDirection.RIGHT_VERTICAL
                        isHorizontal && isBoth && isBottom -> PointerDirection.HORIZONTAL
                        isVertical && isBoth && isLeftVertical -> PointerDirection.LEFT_VERTICAL
                        isVertical && isBoth && isRightVertical -> PointerDirection.RIGHT_VERTICAL
                        else -> PointerDirection.HORIZONTAL_AND_VERTICAL
                    }

                    pointerDirection = direction
                    onDragAmount(change, change.positionChange(), pointerDirection)
                    change.consume()
                }.not() -> onDragCancel()

                else -> onDragEnd()
            }
        }
    }
}