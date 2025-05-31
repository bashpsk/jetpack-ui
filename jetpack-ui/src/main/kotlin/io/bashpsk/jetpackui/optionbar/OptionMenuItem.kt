package io.bashpsk.jetpackui.optionbar

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun OptionMenuItem(
    modifier: Modifier = Modifier,
    optionData: () -> OptionBarData = { OptionBarData() },
    onOptionClick: (option: OptionBarData) -> Unit = {}
) {

    DropdownMenuItem(
        modifier = modifier,
        enabled = optionData().isEnable,
        text = {

            Text(
                text = optionData().label,
                textAlign = TextAlign.Start,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {

            Icon(
                modifier = Modifier.size(size = 20.dp),
                imageVector = optionData().icon,
                contentDescription = optionData().label
            )
        },
        onClick = {

            onOptionClick(optionData())
        }
    )
}