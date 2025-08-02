package io.bashpsk.jetpackuidemo

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.bashpsk.jetpackui.gesture.DragChanges
import io.bashpsk.jetpackui.gesture.TapChanges
import io.bashpsk.jetpackui.gesture.ValueChange
import io.bashpsk.jetpackui.gesture.VideoGestureBox
import io.bashpsk.jetpackui.gesture.VideoGestureConfig
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun VideoGestureDemoScreen() {

    val context = LocalContext.current
    val activity = LocalActivity.current
    val window = activity?.window

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var currentVolume by rememberSaveable {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }

    val maxVolume by remember {
        derivedStateOf { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    }

    var brightness by rememberSaveable(window) {
        mutableFloatStateOf(window?.attributes?.screenBrightness?.coerceIn(0.0F..1.0F) ?: 0.0F)
    }

    var boostedFinish by rememberSaveable { mutableFloatStateOf(0.0F) }
    var imageViewScale by rememberSaveable { mutableFloatStateOf(1.0F) }
    var imageViewOffset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        VideoGestureBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            config = VideoGestureConfig(
                isPanEnable = false
            ),
            onTapChanges = { changes ->

                when (changes) {

                    is TapChanges.BackwardTap -> setDebug("BackwardTap : ${changes.position}")

                    is TapChanges.ForwardTap -> setDebug("ForwardTap : ${changes.position}")

                    is TapChanges.SingleTap -> setDebug("SingleTap : ${changes.position}")

                    is TapChanges.Unknown -> setDebug("Unknown")
                }
            },
            onDragChanges = { changes ->

                when (changes) {

                    is DragChanges.VerticalLeftChanges -> {

                        when (changes.changes) {

                            ValueChange.Increased -> when (brightness >= 1.00F) {

                                true -> brightness = 1.00F
                                false -> brightness += 0.02F
                            }

                            ValueChange.Decreased -> when (brightness <= 0.00F) {

                                true -> brightness = 0.00F
                                false -> brightness -= 0.02F
                            }

                            else -> {}
                        }

                        window?.attributes = window.attributes?.apply {

                            screenBrightness = brightness.coerceIn(0.0F..1.0F)
                            setDebug("Brightness : $screenBrightness")
                        }
                    }

                    is DragChanges.DragCanceled -> setDebug("DragCanceled")

                    is DragChanges.DragEnded -> setDebug("DragEnded")

                    is DragChanges.DragStart -> setDebug("DragStart : ${changes.position}")

                    is DragChanges.HorizontalTopChanges -> {

                        setDebug("HorizontalTopChanges : ${changes.changes}")
                    }

                    is DragChanges.HorizontalBottomChanges -> {

                        setDebug("HorizontalBottomChanges : ${changes.changes}")
                    }

                    is DragChanges.Unknown -> setDebug("Unknown")

                    is DragChanges.VerticalRightChanges -> {

                        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                        when (changes.changes) {

                            ValueChange.Increased -> {

                                val isMaxVolume = currentVolume == maxVolume
                                val isBoost = boostedFinish <= 30.0F
                                val isBoostLeast = boostedFinish == 0.0F

                                when {

                                    isMaxVolume && isBoost -> {

                                        boostedFinish = (boostedFinish + 1.0F).coerceIn(
                                            range = 0.0F..30.0F
                                        )

                                        setDebug("Volume Boosted : $boostedFinish")
                                    }

                                    isMaxVolume && isBoostLeast -> {

                                        boostedFinish = (boostedFinish + 1.0F).coerceIn(
                                            range = 0.0F..30.0F
                                        )

                                        setDebug("Volume Boosted : $boostedFinish")
                                    }

                                    isMaxVolume.not() -> {

                                        audioManager.adjustStreamVolume(
                                            AudioManager.STREAM_MUSIC,
                                            AudioManager.ADJUST_RAISE,
                                            AudioManager.FLAG_PLAY_SOUND
                                        )

                                        currentVolume = audioManager.getStreamVolume(
                                            AudioManager.STREAM_MUSIC
                                        )

                                        setDebug("Volume Normal : $currentVolume")
                                    }

                                    else -> {

                                        audioManager.adjustStreamVolume(
                                            AudioManager.STREAM_MUSIC,
                                            AudioManager.ADJUST_RAISE,
                                            AudioManager.FLAG_PLAY_SOUND
                                        )

                                        currentVolume = audioManager.getStreamVolume(
                                            AudioManager.STREAM_MUSIC
                                        )

                                        setDebug("Volume Normal : $currentVolume")
                                    }
                                }
                            }

                            ValueChange.Decreased -> {

                                val isMaxVolume = currentVolume == maxVolume
                                val isBoost = boostedFinish >= 1.0F
                                val isBoostLeast = boostedFinish == 1.0F

                                when {

                                    isMaxVolume && isBoost -> {

                                        boostedFinish = (boostedFinish - 1.0F).coerceIn(
                                            range = 0.0F..30.0F
                                        )

                                        setDebug("Volume Boosted : $boostedFinish")
                                    }

                                    isMaxVolume && isBoostLeast -> {

                                        boostedFinish = (boostedFinish - 1.0F).coerceIn(
                                            range = 0.0F..30.0F
                                        )

                                        setDebug("Volume Boosted : $boostedFinish")
                                    }

                                    isMaxVolume.not() -> {

                                        audioManager.adjustStreamVolume(
                                            AudioManager.STREAM_MUSIC,
                                            AudioManager.ADJUST_LOWER,
                                            AudioManager.FLAG_PLAY_SOUND
                                        )

                                        currentVolume = audioManager.getStreamVolume(
                                            AudioManager.STREAM_MUSIC
                                        )

                                        setDebug("Volume Normal : $currentVolume")
                                    }

                                    else -> {

                                        audioManager.adjustStreamVolume(
                                            AudioManager.STREAM_MUSIC,
                                            AudioManager.ADJUST_LOWER,
                                            AudioManager.FLAG_PLAY_SOUND
                                        )

                                        currentVolume = audioManager.getStreamVolume(
                                            AudioManager.STREAM_MUSIC
                                        )

                                        setDebug("Volume Normal : $currentVolume")
                                    }
                                }
                            }

                            else -> {}
                        }
                    }

                    is DragChanges.TransformChanges -> {

                        imageViewScale *= changes.zoom
                        imageViewOffset += changes.pan
                    }
                }
            }
        ) {

            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = imageViewScale.coerceIn(range = 0.1F..5.0F),
                        scaleY = imageViewScale.coerceIn(range = 0.1F..5.0F),
                        translationX = imageViewOffset.x,
                        translationY = imageViewOffset.y
                    ),
                painter = painterResource(R.drawable.wallpaper_01),
                contentScale = ContentScale.Fit,
                contentDescription = "Image"
            )

            GesturePreview(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5F)
            )

            GesturePreview(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5F)
                    .rotate(90.0F)
            )
        }
    }
}

@Composable
private fun GesturePreview(modifier: Modifier = Modifier) {

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(space = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        GestureColorBox(
            modifier = Modifier
                .weight(weight = 0.05F)
                .fillMaxHeight(),
            color = Color.Gray
        )

        GestureColorBox(
            modifier = Modifier
                .weight(weight = 0.85F / 2)
                .fillMaxHeight(),
            color = Color.Green
        )

        GestureColorBox(
            modifier = Modifier
                .weight(weight = 0.05F)
                .fillMaxHeight(),
            color = Color.Gray
        )

        GestureColorBox(
            modifier = Modifier
                .weight(weight = 0.85F / 2)
                .fillMaxHeight(),
            color = Color.Yellow
        )

        GestureColorBox(
            modifier = Modifier
                .weight(weight = 0.05F)
                .fillMaxHeight(),
            color = Color.Gray
        )
    }
}

@Composable
private fun GestureColorBox(modifier: Modifier = Modifier, color: Color) {

    Box(
        modifier = modifier.background(color = color)
    )
}