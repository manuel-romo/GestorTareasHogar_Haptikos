package haptikos.gestortareashogar_haptikos.ui.screens.formRoom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole

data class RoleStyles(
    val text: String,
    val containerColor: Color,
    val contentColor: Color,
    val iconRes: Int
)

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
    member: MemberEntity,
    taskCount: Int,
    isCurrentUser: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circular
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(android.graphics.Color.parseColor(member.colorHex)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(member.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${member.name} ${member.lastName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    // Indicador de "Tú".
                    Badge(containerColor = Color(0xFFFFE0B2)) { Text("Tú", color = Color(0xFFE65100)) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberRoleBadge(member.role)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$taskCount tareas ✓", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        // Acciones
        if (member.role != MemberRole.CREATOR) {
            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp).background(Color(0xFFF5F5F5), CircleShape)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pencil),
                        contentDescription = "Editar",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp).background(Color(0xFFFFEBEE), CircleShape)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_user_cross),
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
