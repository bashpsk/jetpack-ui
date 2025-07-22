package io.bashpsk.jetpackuidemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.bashpsk.jetpackuidemo.ui.screen.VideoGestureDemoScreen
import io.bashpsk.jetpackuidemo.ui.theme.JetpackUITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            JetpackUITheme {

//                DemoScreen()
                VideoGestureDemoScreen()
            }
        }
    }
}