package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    // Estados internos del formulario
    var roomName by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) } // Guardamos el Hex

    // Datos fijos
    // TODO traer de BD
    val emojis = listOf(
        "🍳", "🛋️", "🚿", "🛏️", "🧺", "🌱", "🚗", "🏠",
        "📦", "🎮", "📚", "🍽️", "🧹", "🪞", "🛁", "🖥️",
        "🎵", "🌿", "🪴", "🗄️"
    )

    // Paleta de Colores
    // TODO traer de BD
    val roomColorsHex = listOf(
        "0xFFFFE0B2",
        "0xFFBBDEFB",
        "0xFFE1BEE7",
        "0xFFC8E6C9",
        "0xFFF8BBD0",
        "0xFFFFF9C4",
        "0xFFB2EBF2",
        "0xFFD1C4E9"
    )

    val textPreviewColor = Color(0xFFE65100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Editar habitación",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF5F5F5), CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cross),
                    contentDescription = "Cerrar",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de Nombre
        Text(
            text = "NOMBRE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de Selección de Icono
        Text(
            text = "ICONO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(170.dp)
        ) {
            items(emojis) { emoji ->
                val isSelected = selectedIcon == emoji
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) parseHexColor(selectedColorHex) else Color(0xFFF5F5F5)
                        )
                        .clickable { selectedIcon = emoji }
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de Color
        Text(
            text = "COLOR",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lista de Strings
            roomColorsHex.forEach { hexCode ->
                val isSelected = selectedColorHex == hexCode
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(hexCode))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color.Gray.copy(alpha = 0.5f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColorHex = hexCode }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                modifier = Modifier
                    .size(40.dp)
                    .background(parseHexColor(selectedColorHex), CircleShape)
            ) {
                Text(text = selectedIcon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = roomName.ifBlank { "Nombre de habitación" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPreviewColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Guardar
        Button(
            onClick = { onSave(roomName, selectedIcon, selectedColorHex) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF7A00)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}