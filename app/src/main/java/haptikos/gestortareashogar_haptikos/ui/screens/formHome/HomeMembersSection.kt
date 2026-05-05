package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew

data class RoleStyles(
    val text: String,
    val containerColor: Color,
    val contentColor: Color,
    val iconRes: Int
)

@Composable
fun HomeMembersSection(members: List<MemberEntityNew>) {
    // Estados para control de diálogos
    var memberToEdit by remember { mutableStateOf<MemberEntityNew?>(null) }
    var memberToDelete by remember { mutableStateOf<MemberEntityNew?>(null) }


    Column {
        SectionTitleHeader(icon = R.drawable.ic_users, title = "MIEMBROS DEL HOGAR")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                members.forEachIndexed { index, member ->
                    val isCurrentUser = index == 0

                    MemberItem(
                        member = member,
                        taskCount = (20..50).random(),
                        isCurrentUser = isCurrentUser,
                        onEditClick = { memberToEdit = member },
                        onDeleteClick = { memberToDelete = member }
                    )

                    if (index < members.size - 1) {
                        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    // Edición
    memberToEdit?.let { member ->
        EditRoleBottomSheet(
            member = member,
            onDismissRequest = { memberToEdit = null },
            onSaveRole = { newRole ->
                // TODO: Actualizar rol en ViewModel
                memberToEdit = null
            }
        )
    }

    // Eliminación
    memberToDelete?.let { member ->
        ConfirmDeleteBottomSheet(
            title = "¿Eliminar a ${member.name}?",
            description = "Al eliminar a este miembro, perderá inmediatamente el acceso al hogar. Todas sus tareas pendientes quedarán sin asignar.",
            iconRes = R.drawable.ic_user_cross,
            cancelButtonText = "Mantener",
            confirmButtonText = "Sí, eliminar",
            onDismissRequest = { memberToDelete = null },
            onConfirmDelete = {
                // TODO eliminación de miembro
                memberToDelete = null
            }
        )
    }
}


@Composable
fun MemberRoleBadge(role: MemberRole) {
    val styles = when (role) {
        MemberRole.CREATOR -> RoleStyles(
            "Creador",
            Color(0xFFFFECB3),
            Color(0xFFFFA000),
            R.drawable.ic_star
        )
        MemberRole.ADMIN -> RoleStyles(
            "Administrador",
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            R.drawable.ic_shield
        )
        MemberRole.MEMBER -> RoleStyles(
            "Miembro",
            Color(0xFFF5F5F5),
            Color(0xFF757575),
            R.drawable.ic_user
        )
    }

    Surface(
        color = styles.containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                painterResource(styles.iconRes),
                null,
                modifier = Modifier
                    .size(14.dp),
                tint = styles.contentColor)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = styles.text,
                color = styles.contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MemberItem(
    member: MemberEntityNew,
    taskCount: Int,
    isCurrentUser: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circular
        Box(
            modifier = Modifier.size(48.dp).background(Color(android.graphics.Color.parseColor(member.colorHex)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(member.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = Color(0xFFFFE0B2), shape = RoundedCornerShape(4.dp)) {
                        Text(" Tú ", color = Color(0xFFE65100), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberRoleBadge(member.role)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$taskCount tareas ✓", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        if (member.role != MemberRole.CREATOR) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), CircleShape).clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_pencil), "Editar", modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
                Box(
                    modifier = Modifier.size(32.dp).background(Color(0xFFFFEBEE), CircleShape).clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_user_cross), "Eliminar", modifier = Modifier.size(16.dp), tint = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoleBottomSheet(
    member: MemberEntityNew,
    onDismissRequest: () -> Unit,
    onSaveRole: (MemberRole) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Estado local para rol seleccionado antes de guardar
    var selectedRole by remember { mutableStateOf(member.role) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Título y botón cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cambiar rol de ${member.name}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .clickable { onDismissRequest() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cross),
                        contentDescription = "Cerrar",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta del usuario
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(android.graphics.Color.parseColor(member.colorHex)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(member.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("ROL EN EL HOGAR", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Botones de selección de Rol
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoleSelectionButton(
                    modifier = Modifier.weight(1f),
                    title = "Administrador",
                    icon = R.drawable.ic_shield,
                    isSelected = selectedRole == MemberRole.ADMIN,
                    onClick = { selectedRole = MemberRole.ADMIN }
                )
                RoleSelectionButton(
                    modifier = Modifier.weight(1f),
                    title = "Miembro",
                    icon = R.drawable.ic_user,
                    isSelected = selectedRole == MemberRole.MEMBER,
                    onClick = { selectedRole = MemberRole.MEMBER }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción de Rol
            val description = if (selectedRole == MemberRole.ADMIN) {
                "✏️ Puede editar tareas, habitaciones y gestionar miembros."
            } else {
                "✅ Solo puede ver y completar sus tareas asignadas."
            }
            Text(text = description, color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Guardar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFFFF8A00), RoundedCornerShape(16.dp))
                    .clickable { onSaveRole(selectedRole) },
                contentAlignment = Alignment.Center
            ) {
                Text("✓ Guardar cambios", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RoleSelectionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFF8A00) else Color(0xFFE0E0E0)
    val contentColor = if (isSelected) Color(0xFFFF8A00) else Color.Gray

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(icon), null, modifier = Modifier.size(16.dp), tint = contentColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}