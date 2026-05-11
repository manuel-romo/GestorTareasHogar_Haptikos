package haptikos.gestortareashogar_haptikos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationList(
    notifyTaskReminders: Boolean,
    notifyTaskCompleted: Boolean,
    notifyNewMembers: Boolean,
    onRemindersChange: (Boolean) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    onNewMembersChange: (Boolean) -> Unit,
    isEnabled: Boolean = true
) {
    val contentAlpha = if (isEnabled) 1f else 0.5f

    Column(modifier = Modifier.graphicsLayer(alpha = contentAlpha)) {
        NotificationEmojiRow(
            emoji = "⏰",
            title = "Recordatorios",
            desc = "Aviso antes de que venza una tarea",
            isChecked = notifyTaskReminders,
            onCheckedChange = onRemindersChange,
            enabled = isEnabled
        )
        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))

        NotificationEmojiRow(
            emoji = "✅",
            title = "Tareas completadas",
            desc = "Cuando un miembro completa una tarea",
            isChecked = notifyTaskCompleted,
            onCheckedChange = onCompletedChange,
            enabled = isEnabled
        )
        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))

        NotificationEmojiRow(
            emoji = "👋",
            title = "Nuevos miembros",
            desc = "Cuando alguien se une al hogar",
            isChecked = notifyNewMembers,
            onCheckedChange = onNewMembersChange,
            enabled = isEnabled
        )
    }
}

@Composable
fun NotificationEmojiRow(
    emoji: String,
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Text(text = emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = if(enabled) Color.Black else Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Text(desc, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00C853),
                disabledCheckedTrackColor = Color(0xFF00C853).copy(alpha = 0.3f)
            )
        )
    }
}