package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import java.text.SimpleDateFormat
import java.util.*
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray


// Modelo de opción de pausa
private data class PauseOption(
    val label: String,
    val emoji: String,
    val days: Int
)

private val PAUSE_OPTIONS = listOf(
    PauseOption("1 día", "☀️",1),
    PauseOption("3 días", "🗓️", 3),
    PauseOption("1 semana", "📅", 7),
    PauseOption("2 semanas", "📆", 14),
    PauseOption("1 mes", "🟡",  30)
)


private fun formatDate(epochMs: Long): String {
    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    return sdf.format(Date(epochMs))
}

private fun addDays(days: Int): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, days)
    return cal.timeInMillis
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PauseTaskBottomSheet(
    taskTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (pausedUntil: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Opción predefinida seleccionada
    var selectedOption by remember { mutableStateOf<PauseOption?>(null) }

    // Fecha personalizada elegida con DatePicker
    var customDate by remember { mutableStateOf<Long?>(null) }

    // Mostrar DatePickerDialog
    var showDatePicker by remember { mutableStateOf(false) }

    val resolvedEpoch: Long? = when {
        customDate != null   -> customDate
        selectedOption != null -> addDays(selectedOption!!.days)
        else                 -> null
    }

    val canConfirm = resolvedEpoch != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {

            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pausar tarea",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = "Cerrar",
                        tint = MediumDarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "La tarea no aparecerá como pendiente para los miembros asignados durante el periodo seleccionado.",
                style = MaterialTheme.typography.bodySmall,
                color = MediumDarkGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Label sección
            Text(
                text = "DURACIÓN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Opciones predefinidas
            PAUSE_OPTIONS.forEach { option ->
                val isSelected = selectedOption == option && customDate == null
                val untilEpoch = addDays(option.days)
                val untilText  = "Hasta el ${formatDate(untilEpoch)}"

                PauseOptionRow(
                    emoji = option.emoji,
                    label = option.label,
                    subtitle = untilText,
                    isSelected = isSelected,
                    onClick = {
                        selectedOption = option
                        customDate     = null
                    }
                )
            }

            // Separador y fecha personalizada
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "O ELIGE UNA FECHA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Campo de fecha personalizada
            val customDateText = customDate?.let { formatDate(it) } ?: ""

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = if (customDate != null)
                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                else
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant ?: SilverGray)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (customDate != null) customDateText else "dd/mm/aaaa",
                        color = if (customDate != null) MaterialTheme.colorScheme.onSurface else MediumDarkGray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = "Elegir fecha",
                        tint = if (customDate != null) MaterialTheme.colorScheme.primary else MediumDarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón confirmar
            Button(
                onClick = {
                    resolvedEpoch?.let { onConfirm(it) }
                },
                enabled = canConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pause),
                    contentDescription = null,
                    tint = if (canConfirm) MaterialTheme.colorScheme.onPrimary else MediumDarkGray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirmar pausa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (canConfirm) MaterialTheme.colorScheme.onPrimary else MediumDarkGray
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        customDate     = it
                        selectedOption = null
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = MediumDarkGray)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor      = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun PauseOptionRow(
    emoji: String,
    label: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.size(36.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Textos
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Radio button
        RadioButton(
            selected = isSelected,
            onClick  = onClick,
            colors   = RadioButtonDefaults.colors(
                selectedColor   = MaterialTheme.colorScheme.primary,
                unselectedColor = SilverGray
            )
        )
    }
}