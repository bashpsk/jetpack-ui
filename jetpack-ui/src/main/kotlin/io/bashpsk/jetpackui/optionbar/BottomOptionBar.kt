package io.bashpsk.jetpackui.optionbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ContextualFlowRowOverflowScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomOptionBar(
    modifier: Modifier = Modifier,
    optionList: SnapshotStateList<OptionBarData> = mutableStateListOf(),
    onOptionClick: (option: OptionBarData) -> Unit = {},
    maxLines: Int = 1,
) {

    val isMoreOptionMenuExpanded = remember { mutableStateOf(value = false) }
    val menuPosition = remember { mutableStateOf(value = DpOffset.Zero) }
    val remainingItems = remember { mutableStateListOf<OptionBarData>() }

    val expandIndicator = @Composable { scope: ContextualFlowRowOverflowScope ->

        val remainingItemCount = optionList.size - scope.shownItemCount

        remainingItems.apply {

            val newItems = optionList.subList(
                fromIndex = scope.shownItemCount,
                toIndex = optionList.size
            )

            clear()
            addAll(elements = newItems)
        }

        OptionView(
            optionData = { OptionBarData(label = "More", icon = Icons.Filled.MoreVert) },
            onOptionClick = { operation ->

                isMoreOptionMenuExpanded.value = true
            }
        )
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {

        ContextualFlowRow(
            modifier = modifier
                .align(alignment = Alignment.Center)
                .onPlaced { layoutCoordinates ->

                    menuPosition.value = DpOffset(x = layoutCoordinates.size.width.dp, y = 0.dp)
                },
            maxLines = maxLines,
            itemCount = optionList.size,
            overflow = ContextualFlowRowOverflow.expandOrCollapseIndicator(
                minRowsToShowCollapse = 0,
                expandIndicator = expandIndicator,
                collapseIndicator = {}
            ),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) { itemIndex ->

            OptionView(
                optionData = { optionList[itemIndex] },
                onOptionClick = { operation ->

                    onOptionClick(operation)
                    isMoreOptionMenuExpanded.value = false
                }
            )
        }

        DropdownMenu(
            expanded = isMoreOptionMenuExpanded.value,
            onDismissRequest = {

                isMoreOptionMenuExpanded.value = false
            },
            offset = menuPosition.value
        ) {

            HorizontalDivider()

            remainingItems.forEach { item ->

                DropdownMenuItem(
                    text = {

                        Text(
                            text = item.label,
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {

                        Icon(
                            modifier = Modifier.size(size = 20.dp),
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    onClick = {

                        isMoreOptionMenuExpanded.value = false
                        onOptionClick(item)
                    }
                )
            }

            HorizontalDivider()
        }
    }
}