package haptikos.gestortareashogar_haptikos.ui.screens.formTask

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.ui.enums.TurnMode
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode

@Composable
fun WorkModeSection(
    selectedWorkMode: WorkMode, onWorkModeChange: (WorkMode) -> Unit,
    selectedTurnMode: TurnMode, onTurnModeChange: (TurnMode) -> Unit,
    orderedTurns: List<MemberEntity>, onShuffleTurns: () -> Unit,
    onMoveTurn: (Int, Int) -> Unit
) {
    SectionTitle("MODO DE TRABAJO")
    Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(16.dp), color = LightBg, border = BorderStroke(1.dp, Color(0xFFE5E5EA))) {
        Row(modifier = Modifier.padding(8.dp)) {
            WorkModeButton("En equipo", "🤝", selectedWorkMode == WorkMode.TEAM, OrangeMain, Modifier.weight(1f)) { onWorkModeChange(WorkMode.TEAM) }
            Spacer(modifier = Modifier.width(8.dp))
            WorkModeButton("Dividir días", "✂️", selectedWorkMode == WorkMode.SPLIT, OrangeMain, Modifier.weight(1f)) { onWorkModeChange(WorkMode.SPLIT) }
        }
    }

    if (selectedWorkMode == WorkMode.TEAM) {
        Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF8E1), border = BorderStroke(1.dp, Color(0xFFFFECB3))) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = "🤝", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Todos los miembros realizan la tarea juntos el mismo día.", color = OrangeMain, fontSize = 14.sp)
            }
        }
    } else {
        Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFFFFF3E0), border = BorderStroke(1.dp, Color(0xFFFFCC80))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("¿CÓMO ASIGNAR LOS TURNOS?", color = OrangeMain, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    WorkModeButton("Al azar", "🎲", selectedTurnMode == TurnMode.RANDOM, OrangeMain, Modifier.weight(1f)) { onTurnModeChange(TurnMode.RANDOM) }
                    Spacer(modifier = Modifier.width(8.dp))
                    WorkModeButton("Manual", "✋", selectedTurnMode == TurnMode.MANUAL, OrangeMain, Modifier.weight(1f)) { onTurnModeChange(TurnMode.MANUAL) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("ORDEN DE TURNOS", color = OrangeMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (selectedTurnMode == TurnMode.RANDOM) {
                        Text("Barajar 🎲", color = OrangeMain, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { onShuffleTurns() })
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                orderedTurns.forEachIndexed { index, member ->
                    key(member.name) {
                        ReorderableMemberItem(
                            index = index,
                            member = member,
                            itemsCount = orderedTurns.size,
                            isManual = selectedTurnMode == TurnMode.MANUAL,
                            onMove = onMoveTurn
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedTurnMode == TurnMode.RANDOM) "🎲" else "✋", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedTurnMode == TurnMode.RANDOM) "El orden se generó al azar. Toca «Barajar» para cambiar." else "Arrastra las filas para reordenar los turnos.", color = OrangeMain, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun WorkModeButton(
    text: String,
    emoji: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.White else Color(0xFFF2F2F7),
        border = BorderStroke(1.dp, if (isSelected) activeColor else Color.Transparent)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) activeColor else Color.Black
            )
        }
    }
}

@Composable
fun ReorderableMemberItem(
    index: Int,
    member: MemberEntity,
    itemsCount: Int,
    isManual: Boolean,
    onMove: (Int, Int) -> Unit
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 60.dp.toPx() }

    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val currentIndex by rememberUpdatedState(index)
    val currentItemsCount by rememberUpdatedState(itemsCount)
    val currentOnMove by rememberUpdatedState(onMove)

    // Variable con modificadores base
    var cardModifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)
        .zIndex(if (isDragging) 1f else 0f)
        .graphicsLayer { translationY = offsetY }

    // Se agrega el arrastre solo si está en modo manual
    if (isManual) {
        cardModifier = cardModifier.pointerInput(member.name) {
            detectVerticalDragGestures(
                onDragStart = { _ -> isDragging = true },
                onDragEnd = { isDragging = false; offsetY = 0f },
                onDragCancel = { isDragging = false; offsetY = 0f },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    offsetY += dragAmount
                    if (offsetY > itemHeightPx * 0.6f && currentIndex < currentItemsCount - 1) {
                        currentOnMove(currentIndex, currentIndex + 1)
                        offsetY = 0f
                    } else if (offsetY < -itemHeightPx * 0.6f && currentIndex > 0) {
                        currentOnMove(currentIndex, currentIndex - 1)
                        offsetY = 0f
                    }
                }
            )
        }
    }

    // La variable completa se envía a Surface
    Surface(
        modifier = cardModifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = if (isDragging) 8.dp else 0.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFF2F2F7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Texto de Semana y Nombre
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SEMANA ${index + 1}",
                    color = OrangeMain,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = member.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            if (isManual) {
                Icon(
                    painterResource(id = R.drawable.ic_grab),
                    contentDescription = "Reordenar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}