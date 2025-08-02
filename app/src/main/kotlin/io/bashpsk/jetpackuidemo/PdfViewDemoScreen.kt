package io.bashpsk.jetpackuidemo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.bashpsk.jetpackui.pdfview.PdfSource
import io.bashpsk.jetpackui.pdfview.LazyPdfLayout
import io.bashpsk.jetpackui.pdfview.rememberPdfViewState

@Composable
fun PdfViewDemoScreen() {

    var pdfUri by remember { mutableStateOf<Uri?>(null) }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { resultUri ->

        pdfUri = resultUri
    }

    val pdfViewState = rememberPdfViewState(source = PdfSource.FromUri(pdfUri ?: Uri.EMPTY))

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {

            Button(
                onClick = { documentLauncher.launch(arrayOf("application/pdf")) }
            ) {
                Text("Open PDF from Device")
            }

            LazyPdfLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = pdfViewState,
            )
        }
    }
}