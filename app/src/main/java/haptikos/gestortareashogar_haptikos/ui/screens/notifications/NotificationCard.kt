package haptikos.gestortareashogar_haptikos.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.NotificationEntity
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.Green
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.Purple

@Composable
fun NotificationCard(notification: NotificationEntity) {
    val icon = when (notification.type) {
        "TASK_COMPLETED" -> R.drawable.ic_check_circle
        "NEW_MEMBER" -> R.drawable.ic_user
        "TASK_REMINDER" -> R.drawable.ic_bell
        else -> R.drawable.ic_bell
    }

    val iconColor = when (notification.type) {
        "TASK_COMPLETED" -> Green
        "NEW_MEMBER" -> Purple
        "TASK_REMINDER" -> BrightOrange
        else -> MediumDarkGray
    }

    val timeText = remember(notification.createdAt) {
        val diff = System.currentTimeMillis() - notification.createdAt
        when {
            diff < 3_600_000 -> "hace ${diff / 60_000} min"
            diff < 86_400_000 -> "hace ${diff / 3_600_000} h"
            else -> "hace ${diff / 86_400_000} días"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (!notification.isRead) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.title,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = notification.body,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = timeText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}