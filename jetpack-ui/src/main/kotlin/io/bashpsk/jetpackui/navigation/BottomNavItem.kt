package io.bashpsk.jetpackui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavItem(
    isSelected: () -> Boolean = { false },
    label: () -> String = { "" },
    icon: () -> ImageVector = { Icons.Filled.Info },
    shape: Shape = RoundedCornerShape(size = 0.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onItemClick: () -> Unit = {}
) {

    Surface(
        modifier = Modifier.wrapContentSize(),
        color = containerColor,
        contentColor = contentColor,
        shape = shape,
        enabled = enabled,
        onClick = onItemClick
    ) {

        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {

            Icon(imageVector = icon(), contentDescription = label())

            AnimatedVisibility(visible = isSelected()) {

                Text(
                    text = label(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}