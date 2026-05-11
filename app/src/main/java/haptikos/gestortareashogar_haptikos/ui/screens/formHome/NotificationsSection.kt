package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.ui.components.NotificationList

@Composable
fun NotificationsSection(
    home: HomeEntityNew,
    onUpdate: (HomeEntityNew) -> Unit
) {
    Column {
        SectionTitleHeader(icon = R.drawable.ic_bell, title = "NOTIFICACIONES")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                // Componente global
                NotificationList(
                    notifyTaskReminders = home.notifyTaskReminders,
                    notifyTaskCompleted = home.notifyTaskCompleted,
                    notifyNewMembers = home.notifyNewMembers,
                    onRemindersChange = { onUpdate(home.copy(notifyTaskReminders = it)) },
                    onCompletedChange = { onUpdate(home.copy(notifyTaskCompleted = it)) },
                    onNewMembersChange = { onUpdate(home.copy(notifyNewMembers = it)) },
                    isEnabled = home.notifyAllMembers
                )

                // Sección de control global (Solo visible en Configuración de Hogar)
                Surface(color = Color(0xFFFFF8F0)) {
                    Column {
                        NotificationMasterRow(
                            icon = R.drawable.ic_bell,
                            title = "Notificaciones para todos",
                            desc = "Habilita el envío de alertas en este hogar",
                            isChecked = home.notifyAllMembers,
                            onCheckedChange = { onUpdate(home.copy(notifyAllMembers = it)) },
                            activeColor = Color(0xFFFF8A00)
                        )

                        HorizontalDivider(color = Color(0xFFFFE0B2), modifier = Modifier.padding(horizontal = 16.dp))

                        NotificationMasterRow(
                            icon = R.drawable.ic_padlock,
                            title = "Imponer mi configuración",
                            desc = "Los miembros no podrán ajustar sus alertas",
                            isChecked = home.forceSettings,
                            onCheckedChange = { onUpdate(home.copy(forceSettings = it)) },
                            activeColor = Color(0xFFD84315),
                            enabled = home.notifyAllMembers
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun NotificationMasterRow(
    icon: Int,
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(
                if(enabled) activeColor else Color.LightGray,
                RoundedCornerShape(10.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(icon), null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = if(enabled) activeColor else Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Text(desc, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
                disabledCheckedTrackColor = activeColor.copy(alpha = 0.3f)
            )
        )
    }
}