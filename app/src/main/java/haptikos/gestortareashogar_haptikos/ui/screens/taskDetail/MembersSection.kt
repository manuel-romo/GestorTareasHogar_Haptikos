package haptikos.gestortareashogar_haptikos.ui.screens.taskDetail

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.utils.parseHexColor

@Composable
fun MembersSection(members: List<MemberEntityNew>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        // Un gris muy clarito o blanco
        color = Color(0xFFF8F9FA)
    ) {
        Column(Modifier.padding(20.dp)) {
            // Título de la sección
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_users),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Miembros asignados", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                    Text("${members.size} personas • En equipo", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            // Lista de miembros
            members.forEachIndexed { index, member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar circular
                    val memberColor = parseHexColor(member.colorHex)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(memberColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.first().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Nombre y Rol
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = member.name, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)

                            // Etiqueta de "Yo"
                            // TODO obtener de viewModel
                            if (member.name == "María") {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFFF8A00).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "Tú",
                                        color = Color(0xFFFF8A00),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(text = "Miembro del hogar", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                if (index < members.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}