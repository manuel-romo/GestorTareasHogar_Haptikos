package haptikos.gestortareashogar_haptikos.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.ui.theme.Black
import haptikos.gestortareashogar_haptikos.ui.theme.BlackGray
import haptikos.gestortareashogar_haptikos.ui.theme.Gray
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.White
import haptikos.gestortareashogar_haptikos.ui.theme.Yellow
import haptikos.gestortareashogar_haptikos.viewModel.RewardViewModel
import kotlinx.coroutines.delay

@Composable
fun ChallengeProgressToast(
    events: List<RewardViewModel.ChallengeProgressEvent>,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    var currentIndex by remember(events) { mutableStateOf(0) }
    var visible by remember(events) { mutableStateOf(true) }

    val event = events.getOrNull(currentIndex) ?: return

    LaunchedEffect(events, currentIndex) {
        visible = true
        delay(3000)
        // Si hay más eventos, pasa al siguiente, si no oculta
        if (currentIndex < events.size - 1) {
            visible = false
            delay(300)
            currentIndex++
        } else {
            visible = false
            delay(300)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(350, easing = EaseOutCubic)
        ) + fadeIn(tween(350)),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(250)
        ) + fadeOut(tween(250))
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .widthIn(min = 200.dp, max = 300.dp)
                .padding(end = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(event.icon, fontSize = 20.sp)
                Column {
                    Text(
                        text = if (event.isCompleted) "¡Reto completado!" else event.title,
                        color = Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (event.isCompleted) "+${getChallengePoints(event.title)} pts 🎉"
                        else "${event.current}/${event.total}",
                        color = if (event.isCompleted) Yellow else MediumDarkGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Mapea título a puntos para mostrar en el toast de completado
private fun getChallengePoints(title: String): Int = when (title) {
    "Completa 5 tareas" -> 25
    "Racha de 3 días" -> 15
    "Todo el día" -> 25
    "Alta prioridad" -> 20
    else -> 0
}