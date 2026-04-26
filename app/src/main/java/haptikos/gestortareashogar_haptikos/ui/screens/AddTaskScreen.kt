package haptikos.gestortareashogar_haptikos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun NewTaskStep1Screen() {
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    // false = Todo el hogar, true = Habitación
    var isRoomScope by remember { mutableStateOf(false) }

    val orangeMain = Color(0xFFFF8A00)
    val grayText = Color(0xFF8E8E93)
    val lightBg = Color(0xFFF9F9F9)

    Scaffold(
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { /* Acción Cancelar */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { /* Acción Siguiente */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orangeMain),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Siguiente", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(orangeMain)
                    .padding(top = 32.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Nueva tarea",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                WizardStepper(currentStep = 1)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Input Nombre de la Tarea
                FormLabel(text = "NOMBRE DE LA TAREA *", iconRes = R.drawable.ic_t_text)
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = { Text("Nombre de la tarea", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = orangeMain,
                        unfocusedBorderColor = Color(0xFFE5E5EA),
                        unfocusedContainerColor = lightBg,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                // Input Descripción Breve
                FormLabel(text = "DESCRIPCIÓN BREVE", iconRes = R.drawable.ic_lines)
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    placeholder = { Text("Descripción de la tarea", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = orangeMain,
                        unfocusedBorderColor = Color(0xFFE5E5EA),
                        unfocusedContainerColor = lightBg,
                        focusedContainerColor = Color.White
                    ),
                    maxLines = 4
                )

                // Selector de Alcance
                Text(
                    text = "ALCANCE",
                    color = grayText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ScopeSelector(
                    isRoomScope = isRoomScope,
                    onScopeChange = { isRoomScope = it }
                )

                // Texto de ayuda dinámico
                Row(
                    modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRoomScope) "🚪"
                        else "🏠",
                        fontSize = 14.sp,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRoomScope) "La tarea pertenece a una habitación concreta."
                        else "La tarea aplica a todo el hogar, sin habitación específica.",
                        color = grayText,
                        fontSize = 12.sp
                    )
                }

                // Botón de selección de habitación (Condicional)
                if (isRoomScope) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* TODO: Abrir selector de habitación */ },
                        shape = RoundedCornerShape(16.dp),
                        color = lightBg,
                        border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono de ubicación circular
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFE3F2FD), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_dactilar), // TODO: Icono de Pin/Ubicación
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Habitación", color = grayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Selecciona una habitación", color = Color.LightGray, fontSize = 14.sp)
                            }

                            Icon(
                                painter = painterResource(id = R.drawable.ic_dactilar), // TODO: Icono Flecha Derecha (>)
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Componentes auxiliares
@Composable
fun FormLabel(text: String, iconRes: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFF8E8E93)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFF8E8E93),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ScopeSelector(isRoomScope: Boolean, onScopeChange: (Boolean) -> Unit) {
    val orangeMain = Color(0xFFFF8A00)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF2F2F7)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            // Opción del Hogar
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onScopeChange(false) },
                shape = RoundedCornerShape(8.dp),
                color = if (!isRoomScope) Color.White else Color.Transparent,
                shadowElevation = if (!isRoomScope) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_home),
                        contentDescription = null,
                        tint = if (!isRoomScope) orangeMain else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Todo el hogar",
                        color = if (!isRoomScope) orangeMain else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Opción de Habitación
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onScopeChange(true) },
                shape = RoundedCornerShape(8.dp),
                color = if (isRoomScope) Color.White else Color.Transparent,
                shadowElevation = if (isRoomScope) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_door),
                        contentDescription = null,
                        tint = if (isRoomScope) orangeMain else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Habitación",
                        color = if (isRoomScope) orangeMain else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WizardStepper(currentStep: Int) {
    val steps = listOf("Descripción", "Programación", "Asignar roles", "Resumen")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isCurrent = stepNumber == currentStep
            val isPassed = stepNumber < currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {

                    // Línea izquierda (invisible para el primer elemento)
                    if (index == 0) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        DashedLine(modifier = Modifier.weight(1f))
                    }

                    // Círculo del paso
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = if (isCurrent || isPassed) Color.White else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber.toString(),
                            color = if (isCurrent || isPassed) Color(0xFFFF8A00) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Línea derecha (invisible para el último elemento)
                    if (index == steps.size - 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        DashedLine(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.7f),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DashedLine(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.height(2.dp)) {
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewTaskStep1ScreenPreview() {
    NewTaskStep1Screen()
}

// Enumerador para manejar las prioridades fácilmente
enum class PriorityLevel(val title: String, val points: Int, val mainColor: Color, val dotColor: Color) {
    ALTA("Alta", 15, Color(0xFFFF3B30), Color(0xFFCC2922)),
    MEDIA("Media", 10, Color(0xFFFF9800), Color(0xFFF57C00)),
    BAJA("Baja", 7, Color(0xFF00C853), Color(0xFF009624))
}

@Composable
fun NewTaskStep2Screen() {
    // Estado para la prioridad seleccionada (Por defecto "Media")
    var selectedPriority by remember { mutableStateOf(PriorityLevel.MEDIA) }

    val orangeMain = Color(0xFFFF8A00)
    val grayText = Color(0xFF8E8E93)
    val lightBg = Color(0xFFF9F9F9)

    Scaffold(
        bottomBar = {
            // Barra inferior con Atrás y Siguiente
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { /* Acción Atrás */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Atrás", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { /* Acción Siguiente */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orangeMain),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Siguiente", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // HEADER Y WIZARD STEPPER (Ahora en el paso 2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(orangeMain)
                    .padding(top = 32.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Nueva tarea",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Asegúrate de tener la función WizardStepper del paso anterior
                WizardStepper(currentStep = 2)
            }

            // CONTENIDO DEL PASO 2
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {

                // SECCIÓN: PROGRAMACIÓN
                Text(
                    text = "PROGRAMACIÓN",
                    color = grayText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = lightBg,
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Column {
                        // Fila: Día sugerido
                        ScheduleOptionRow(
                            iconBgColor = Color(0xFFFFF0E0),
                            iconColor = Color(0xFFFF9800),
                            iconRes = R.drawable.ic_dactilar, // TODO: Cambiar por icono de Calendario
                            title = "Día sugerido",
                            value = "Lunes",
                            valuePrefix = "🌅 " // Emoji de amanecer como en tu diseño
                        )

                        // Divisor sutil
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        // Fila: Recurrencia
                        ScheduleOptionRow(
                            iconBgColor = Color(0xFFF3E5F5),
                            iconColor = Color(0xFF9C27B0),
                            iconRes = R.drawable.ic_dactilar, // TODO: Cambiar por icono de Recurrencia (Flechas en círculo)
                            title = "Recurrencia",
                            value = "Diario",
                            valuePrefix = "🔁 " // Emoji de repetir
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // SECCIÓN: PRIORIDAD
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRIORIDAD",
                        color = grayText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dactilar), // TODO: Icono Estrella pequeña
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Afecta los puntos ganados",
                            color = grayText,
                            fontSize = 12.sp
                        )
                    }
                }

                // Selector de prioridades (Alta, Media, Baja)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PriorityLevel.values().forEach { priority ->
                        PriorityCard(
                            priority = priority,
                            isSelected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun ScheduleOptionRow(
    iconBgColor: Color,
    iconColor: Color,
    iconRes: Int,
    title: String,
    value: String,
    valuePrefix: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Abrir selector */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono redondeado
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBgColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Textos
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color(0xFF8E8E93), fontSize = 12.sp)
            Text(
                text = "$valuePrefix$value",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Flecha derecha
        Icon(
            painter = painterResource(id = R.drawable.ic_dactilar), // TODO: Cambiar por icono Flecha Derecha (>)
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun PriorityCard(
    priority: PriorityLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Definimos los colores dinámicos basados en si está seleccionado o no
    val backgroundColor = if (isSelected) priority.mainColor else Color.White
    val textColor = if (isSelected) Color.White else priority.mainColor
    val borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E5EA)
    val pointsColor = if (isSelected) Color.White else Color.Gray
    val starColor = if (isSelected) Color.White else Color(0xFFFFC107)

    Surface(
        modifier = modifier
            .height(110.dp) // Altura fija para que las 3 cartas sean iguales
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // El circulito de color arriba
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = if (isSelected) priority.dotColor else priority.mainColor,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Título (Alta, Media, Baja)
            Text(
                text = priority.title,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Puntos con la estrellita
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dactilar), // TODO: Icono Estrella
                    contentDescription = null,
                    tint = starColor,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${priority.points} pts",
                    color = pointsColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}