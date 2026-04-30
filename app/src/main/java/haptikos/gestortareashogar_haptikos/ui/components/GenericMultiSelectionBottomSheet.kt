package haptikos.gestortareashogar_haptikos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericMultiSelectionBottomSheet(
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    description: String? = null,
    items: List<T>,
    selectedItems: Set<T>,
    onItemToggled: (T) -> Unit,
    onConfirm: () -> Unit,
    itemBgColorHex: (T) -> String,
    itemText: (T) -> String
) {
    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Título y descripción
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C202E))
                    if (description != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = description, color = Color.Gray, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                HorizontalDivider(color = Color(0xFFF2F2F7), thickness = 1.dp)

                // Lista de items
                items.forEachIndexed { _, item ->
                    val isSelected = selectedItems.contains(item)
                    val hexString = itemBgColorHex(item)
                    val avatarColor = try {
                        Color(android.graphics.Color.parseColor(hexString))
                    } catch (e: Exception) {
                        Color.Gray
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color(0xFFF0FDF4) else Color.White)
                            .clickable { onItemToggled(item) }
                            .padding(vertical = 12.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(avatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = itemText(item).take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = itemText(item),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C202E),
                            modifier = Modifier.weight(1f)
                        )

                        // Checkbox
                        if (isSelected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check_circle),
                                contentDescription = "Seleccionado",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(2.dp, Color(0xFFE5E5EA), CircleShape)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF2F2F7), thickness = 1.dp)
                }

                // Botón de Confirmar
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            disabledContainerColor = Color.LightGray
                        ),
                        enabled = selectedItems.isNotEmpty()
                    ) {
                        Text(
                            text = "Confirmar (${selectedItems.size} miembro${if (selectedItems.size != 1) "s" else ""})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}