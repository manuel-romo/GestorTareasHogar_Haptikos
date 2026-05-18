package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.DarkText
import haptikos.gestortareashogar_haptikos.ui.theme.DeepOrange
import haptikos.gestortareashogar_haptikos.ui.theme.LightSilver
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.SmokeGray
import haptikos.gestortareashogar_haptikos.ui.theme.White
import haptikos.gestortareashogar_haptikos.utils.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomBottomSheet(
    initialName: String = "Cocina",
    initialIcon: String = "🍳",
    initialColorHex: String = "0xFFFFE0B2",
    onDismiss: () -> Unit,
    onSave: (newName: String, newIcon: String, newColorHex: String) -> Unit
) {
    var roomName by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }

    val emojis = listOf(
        "🍳", "🛋️", "🚿", "🛏️", "🧺", "🌱", "🚗", "🏠",
        "📦", "🎮", "📚", "🍽️", "🧹", "🪞", "🛁", "🖥️",
        "🎵", "🌿", "🪴", "🗄️"
    )

    val roomColorsHex = listOf(
        "0xFFFFE0B2", "0xFFBBDEFB", "0xFFE1BEE7", "0xFFC8E6C9",
        "0xFFF8BBD0", "0xFFFFF9C4", "0xFFB2EBF2", "0xFFD1C4E9"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Editar habitación", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp).background(SmokeGray, CircleShape)
            ) {
                Icon(painterResource(R.drawable.ic_cross), "Cerrar", tint = MediumDarkGray, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de Nombre
        Text("NOMBRE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MediumDarkGray, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = LightSilver,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de Selección de Icono
        Text("ICONO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MediumDarkGray, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            emojis.forEach { emoji ->
                val isSelected = selectedIcon == emoji
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) parseHexColor(selectedColorHex) else SmokeGray)
                        .clickable { selectedIcon = emoji }
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de Color
        Text("COLOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MediumDarkGray, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            roomColorsHex.forEach { hexCode ->
                val isSelected = selectedColorHex == hexCode
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(hexCode))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MediumDarkGray.copy(alpha = 0.5f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColorHex = hexCode }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de Preview de Habitación
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(parseHexColor(selectedColorHex).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).background(parseHexColor(selectedColorHex), CircleShape)
            ) {
                Text(text = selectedIcon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = roomName.ifBlank { "Nombre de habitación" },
                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepOrange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Guardar
        Button(
            onClick = { onSave(roomName, selectedIcon, selectedColorHex) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrightOrange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
        }
    }
}