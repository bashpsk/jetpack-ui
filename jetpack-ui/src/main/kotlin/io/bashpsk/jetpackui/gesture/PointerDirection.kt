package io.bashpsk.jetpackui.gesture

internal enum class PointerDirection {

    HorizontalTop,
    HorizontalBottom,
    VerticalLeft,
    VerticalRight,
    Unknown;


    fun hasHorizontal(): Boolean {

        return this == HorizontalTop || this == HorizontalBottom
    }

    fun hasVertical(): Boolean {

        return this == VerticalLeft || this == VerticalRight
    }
}