package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import android.graphics.Color.parseColor
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
import androidx.compose.runtime.collectAsState
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
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.ui.components.MemberAvatar
import haptikos.gestortareashogar_haptikos.ui.theme.Black
import haptikos.gestortareashogar_haptikos.ui.theme.DarkAmber
import haptikos.gestortareashogar_haptikos.ui.theme.DarkBlue
import haptikos.gestortareashogar_haptikos.ui.theme.Gray
import haptikos.gestortareashogar_haptikos.ui.theme.LightAmber
import haptikos.gestortareashogar_haptikos.ui.theme.LightBlue
import haptikos.gestortareashogar_haptikos.ui.theme.LightRed
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.PaleGray
import haptikos.gestortareashogar_haptikos.ui.theme.Red
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray
import haptikos.gestortareashogar_haptikos.ui.theme.SmokeGray
import haptikos.gestortareashogar_haptikos.ui.theme.White
import haptikos.gestortareashogar_haptikos.ui.theme.WhiteGray
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel.MemberModel

data class RoleStyles(
    val text: String,
    val containerColor: Color,
    val contentColor: Color,
    val iconRes: Int
)

@Composable
fun MembersSection(
    members: List<MemberModel>,
    memberViewModel: MemberViewModel,
    homeId: String,
    isCreator: Boolean
) {
    var memberToEdit by remember { mutableStateOf<MemberEntityNew?>(null) }
    var memberToDelete by remember { mutableStateOf<MemberEntityNew?>(null) }

    val memberIds = remember(members) { members.map { it.member.id } }

    val taskCounts by remember(memberIds) {
        memberViewModel.getCompletedTaskCountsForMembers(memberIds)
    }.collectAsState(initial = emptyMap())

    Column {
        SectionTitleHeader(icon = R.drawable.ic_users, title = "MIEMBROS DEL HOGAR")
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                members.forEachIndexed { index, model ->
                    MemberItem(
                        member = model.member,
                        taskCount = taskCounts[model.member.id] ?: 0,
                        isCurrentUser = model.isCurrentUser,
                        isCreator = isCreator,
                        onEditClick = { memberToEdit = model.member },
                        onDeleteClick = { memberToDelete = model.member }
                    )
                    if (index < members.size - 1) {
                        HorizontalDivider(color = WhiteGray, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    memberToEdit?.let { member ->
        EditRoleBottomSheet(
            member = member,
            onDismissRequest = { memberToEdit = null },
            onSaveRole = { newRole ->
                memberViewModel.updateMemberRole(member.id, homeId, newRole)
                memberToEdit = null
            }
        )
    }

    memberToDelete?.let { member ->
        ConfirmDeleteBottomSheet(
            title = "¿Eliminar a ${member.name}?",
            description = "Al eliminar a este miembro, perderá inmediatamente el acceso al hogar. Todas sus tareas pendientes quedarán sin asignar.",
            iconRes = R.drawable.ic_user_cross,
            cancelButtonText = "Mantener",
            confirmButtonText = "Sí, eliminar",
            onDismissRequest = { memberToDelete = null },
            onConfirmDelete = {
                memberViewModel.removeMemberFromHome(member.id, homeId)
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
            LightAmber,
            DarkAmber,
            R.drawable.ic_star
        )
        MemberRole.ADMIN -> RoleStyles(
            "Administrador",
            LightBlue,
            DarkBlue,
            R.drawable.ic_shield
        )
        MemberRole.MEMBER -> RoleStyles(
            "Miembro",
            SmokeGray,
            MediumDarkGray,
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
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = styles.contentColor
            )
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
    isCreator: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    val avatarColor = remember(member.colorHex) {
        try {
            Color(parseColor(member.colorHex))
        } catch (e: Exception) {
            Gray
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberAvatar(member = member, size = 48.dp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = " Tú ",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberRoleBadge(member.role)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$taskCount tareas ✓", style = MaterialTheme.typography.bodySmall, color = MediumDarkGray)
            }
        }

        if (isCreator && member.role != MemberRole.CREATOR) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = SmokeGray,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onEditClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_pencil), "Editar", modifier = Modifier.size(16.dp), tint = MediumDarkGray)
                    }
                }

                Surface(
                    color = LightRed,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onDeleteClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_user_cross), "Eliminar", modifier = Modifier.size(16.dp), tint = Red)
                    }
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
    var selectedRole by remember(member.role) { mutableStateOf(member.role) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cambiar rol de ${member.name}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )

                Surface(
                    color = SmokeGray,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onDismissRequest() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cross),
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(16.dp),
                            tint = MediumDarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val avatarColor = remember(member.colorHex) {
                try { Color(parseColor(member.colorHex)) } catch (e: Exception) { Gray }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaleGray, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(avatarColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.name.take(1).uppercase(), color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(member.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("ROL EN EL HOGAR", color = MediumDarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

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

            val description = if (selectedRole == MemberRole.ADMIN) {
                "✏️ Puede editar tareas, habitaciones y gestionar miembros."
            } else {
                "✅ Solo puede ver y completar sus tareas asignadas."
            }
            Text(text = description, color = MediumDarkGray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSaveRole(selectedRole) },
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓ Guardar cambios", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
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
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else SilverGray
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MediumDarkGray

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
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