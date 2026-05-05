package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRoleBottomSheet(
    member: MemberEntity,
    onDismiss: () -> Unit,
    onSave: (MemberRole) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedRole by remember { mutableStateOf(member.role) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Cambiar rol de ${member.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Información de miembro
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Color(android.graphics.Color.parseColor(member.colorHex)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(member.name.take(1), color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${member.name} ${member.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ROL EN EL HOGAR",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )

            // Selector de Roles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleOptionCard(
                    roleName = "Administrador",
                    iconRes = R.drawable.ic_shield,
                    isSelected = selectedRole == MemberRole.ADMIN,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedRole = MemberRole.ADMIN }
                )
                RoleOptionCard(
                    roleName = "Miembro",
                    iconRes = R.drawable.ic_user,
                    isSelected = selectedRole == MemberRole.MEMBER,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedRole = MemberRole.MEMBER }
                )
            }

            // Checkbox informativo
            Row(
                modifier = Modifier.padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✅ Solo puede ver y completar sus tareas asignadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Botón de Acción Principal
            Button(
                onClick = { onSave(selectedRole) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8000)), // Naranja
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar cambios", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun RoleOptionCard(
    roleName: String,
    iconRes: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFF8000) else Color(0xFFF0F0F0)
    val contentColor = if (isSelected) Color(0xFFFF8000) else Color.Gray
    val backgroundColor = if (isSelected) Color(0xFFFFF3E0) else Color.White

    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        color = backgroundColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = roleName,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}