package io.bashpsk.jetpackui.gesture

import androidx.compose.runtime.Immutable

@Immutable
data class VideoGestureConfig(
    val isDoubleTapEnable: Boolean = true,
    val isHorizontalTopEnable: Boolean = true,
    val isHorizontalBottomEnable: Boolean = true,
    val isVerticalLeftEnable: Boolean = true,
    val isVerticalRightEnable: Boolean = true,
    val isZoomEnable: Boolean = true,
    val isPanEnable: Boolean = true,
    val horizontalTopMinimumSwipe: Int = 25,
    val horizontalBottomMinimumSwipe: Int = 25,
    val verticalLeftMinimumSwipe: Int = 25,
    val verticalRightMinimumSwipe: Int = 25,
    val gestureMargin: Int = 5
)