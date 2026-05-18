package haptikos.gestortareashogar_haptikos.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.viewModel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()

    val grouped = remember(notifications) {
        val now = System.currentTimeMillis()
        val oneDayMs = 86_400_000L
        val twoDaysMs = 2 * oneDayMs

        val today = notifications.filter { now - it.createdAt < oneDayMs }
        val yesterday = notifications.filter {
            now - it.createdAt in oneDayMs until twoDaysMs
        }
        val older = notifications.filter { now - it.createdAt >= twoDaysMs }

        listOf(
            "HOY" to today,
            "AYER" to yesterday,
            "ANTERIORES" to older
        ).filter { it.second.isNotEmpty() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ).statusBarsPadding()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // Botón Atrás
                Surface(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onBack() }
                        .align(Alignment.CenterStart)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Títulos Centrales
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = "Notificaciones",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = "$unreadCount sin leer",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }

                // Botón Marcar Todo
                Surface(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.markAllAsRead() }
                        .align(Alignment.CenterEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_double_check),
                            contentDescription = "Marcar todo como leído",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Filtros
            val filters = listOf(
                NotificationViewModel.NotificationFilter.ALL to "Todas",
                NotificationViewModel.NotificationFilter.UNREAD to "Sin leer ($unreadCount)",
                NotificationViewModel.NotificationFilter.TASK_COMPLETED to "Completadas",
                NotificationViewModel.NotificationFilter.NEW_MEMBER to "Miembros",
                NotificationViewModel.NotificationFilter.TASK_REMINDER to "Recordatorios"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { (filterType, label) ->
                    val selected = currentFilter == filterType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                            )
                            .clickable { viewModel.setFilter(filterType) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Lista
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            grouped.forEach { (label, items) ->
                item {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
                items(items, key = { it.id }) { notification ->
                    SwipeableNotificationCard(
                        notification = notification,
                        onMarkAsRead = { viewModel.markAsRead(notification.id) },
                        onDelete = { viewModel.delete(notification.id) }
                    )
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}