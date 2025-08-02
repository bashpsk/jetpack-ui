package io.bashpsk.jetpackuidemo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.bashpsk.jetpackui.optionbar.BottomOptionBar

@Composable
fun DemoScreen() {

    val mainViewModel = viewModel<DemoViewModel>()

    val selectedPaths by mainViewModel.selectedPaths.collectAsStateWithLifecycle()
    val optionList by mainViewModel.optionList.collectAsStateWithLifecycle()
    val isPathSelect by mainViewModel.isPathSelect.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {

            AnimatedVisibility(
                visible = isPathSelect,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {

                BottomOptionBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(insets = BottomAppBarDefaults.windowInsets),
                    optionList = optionList,
                    onOptionClick = { option ->

                        when (option) {

                            FileOperation.SHARE -> {}
                            
                            FileOperation.SELECT_FILES -> {}
                        }
                    }
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {

                val isSelected by remember {
                    derivedStateOf { selectedPaths.any { path -> path == "Path 1" } }
                }

                Button(
                    enabled = isSelected.not(),
                    onClick = {

                        mainViewModel.addPathSelection("Path 1")
                    }
                ) {

                    Text("Path 1")
                }
            }

            item {

                val isSelected by remember {
                    derivedStateOf { selectedPaths.any { path -> path == "Path 2" } }
                }

                Button(
                    enabled = isSelected.not(),
                    onClick = {

                        mainViewModel.addPathSelection("Path 2")
                    }
                ) {

                    Text("Path 2")
                }
            }

            item {

                val isSelected by remember {
                    derivedStateOf { selectedPaths.any { path -> path == "Path 3" } }
                }

                Button(
                    enabled = isSelected.not(),
                    onClick = {

                        mainViewModel.addPathSelection("Path 3")
                    }
                ) {

                    Text("Path 3")
                }
            }
        }
    }
}