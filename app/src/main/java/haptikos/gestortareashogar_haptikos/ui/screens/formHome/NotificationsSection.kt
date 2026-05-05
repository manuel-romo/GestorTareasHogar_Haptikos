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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun NotificationsSection() {
    Column {
        SectionTitleHeader(icon = R.drawable.ic_bell, title = "NOTIFICACIONES")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                NotificationEmojiRow(
                    emoji = "⏰",
                    title = "Recordatorios de tareas", desc = "Aviso antes de que venza una tarea",
                    initialState = true
                )
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                NotificationEmojiRow(
                    emoji = "✅",
                    title = "Tareas completadas", desc = "Cuando un miembro completa una tarea",
                    initialState = true
                )
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                NotificationEmojiRow(
                    emoji = "👋",
                    title = "Nuevos miembros", desc = "Cuando alguien se une al hogar",
                    initialState = true
                )

                Surface(color = Color(0xFFFFF8F0)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).background(Color(0xFFFF8A00), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painterResource(R.drawable.ic_bell), null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notificaciones para todos", fontWeight = FontWeight.Bold, color = Color(0xFFD84315), style = MaterialTheme.typography.bodyMedium)
                            Text("Todos los miembros reciben notificaciones", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }

                        var isChecked by remember { mutableStateOf(true) }
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFFF8A00)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationEmojiRow(
    emoji: String,
    title: String,
    desc: String,
    initialState: Boolean
) {
    var isChecked by remember { mutableStateOf(initialState) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
            Text(desc, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00C853)
            )
        )
    }
}