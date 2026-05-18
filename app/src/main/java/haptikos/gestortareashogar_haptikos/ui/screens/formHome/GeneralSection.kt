package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
import haptikos.gestortareashogar_haptikos.ui.components.GenericSelectionBottomSheet
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme


@Composable
fun GeneralSection(
    homeName: String,
    editPermission: HomePermission,
    onNameSave: (String) -> Unit,
    onPermissionChange: (HomePermission) -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }

    var currentName by remember(homeName) { mutableStateOf(homeName) }

    var showPermissionsSheet by remember { mutableStateOf(false) }

    var selectedPermission by remember(editPermission) { mutableStateOf(editPermission) }

    Column {
        SectionTitleHeader(icon = R.drawable.ic_shield, title = "GENERAL")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                androidx.compose.animation.Crossfade(
                    targetState = isEditingName,
                    label = "edit_name_anim"
                ) { editing ->
                    if (editing) {
                        Column {
                            Text("Nombre del hogar", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = currentName,
                                    onValueChange = { currentName = it },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                // Botón Guardar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (currentName.isNotBlank()) {
                                                isEditingName = false
                                                onNameSave(currentName)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = "Guardar",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))

                                // Botón Cancelar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                        .clickable {
                                            isEditingName = false
                                            currentName = homeName
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_cross),
                                        contentDescription = "Cancelar",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Nombre del hogar", color = Color.Gray, fontSize = 12.sp)
                                Text(currentName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF5F5F5), CircleShape)
                                    .clickable { isEditingName = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(painterResource(R.drawable.ic_pencil), null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                // Permisos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPermissionsSheet = true }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Permisos de edición", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "${selectedPermission.emoji} ${selectedPermission.title}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(painterResource(R.drawable.ic_arrow_right), null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
            }
        }
    }

    GenericSelectionBottomSheet(
        showSheet = showPermissionsSheet,
        onDismissRequest = { showPermissionsSheet = false },
        title = "Permisos de edición",
        description = "Define quién puede crear, editar y eliminar tareas en este hogar.",
        items = HomePermission.values().toList(),
        selectedItem = selectedPermission,
        onItemSelected = {
            selectedPermission = it
            onPermissionChange(it)
        },
        itemIcon = { it.emoji },
        itemText = { it.title },
        itemSubtitle = { it.description }
    )
}
