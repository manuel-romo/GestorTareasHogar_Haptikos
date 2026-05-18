package haptikos.gestortareashogar_haptikos.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.NotificationEntity
import haptikos.gestortareashogar_haptikos.ui.theme.Blue
import haptikos.gestortareashogar_haptikos.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNotificationCard(
    notification: NotificationEntity,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {

    val density = LocalDensity.current

    val swipeState = remember {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = density,
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onMarkAsRead(); false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        onDelete(); true
                    }
                    else -> false
                }
            },
            positionalThreshold = { totalDistance -> totalDistance * 0.5f }
        )
    }

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            val direction = swipeState.dismissDirection
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    else -> Arrangement.Center
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Blue)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Leído", color = White, fontSize = 12.sp)
                            }
                        }
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.error)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_trash),
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Eliminar", color = White, fontSize = 12.sp)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    ) {
        NotificationCard(notification = notification)
    }
}