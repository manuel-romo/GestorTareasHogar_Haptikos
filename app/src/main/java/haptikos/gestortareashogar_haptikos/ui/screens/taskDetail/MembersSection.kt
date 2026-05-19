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
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.ui.components.MemberAvatar
import haptikos.gestortareashogar_haptikos.ui.theme.DarkText
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray
import haptikos.gestortareashogar_haptikos.ui.theme.White
import haptikos.gestortareashogar_haptikos.utils.parseHexColor

@Composable
fun MembersSection(members: List<MemberEntityNew>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = R.drawable.ic_users), null, tint = MediumDarkGray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Miembros asignados", fontWeight = FontWeight.Bold, color = MediumDarkGray, fontSize = 12.sp)
                    Text("${members.size} personas • En equipo", fontSize = 14.sp, color = DarkText, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = SilverGray)
            Spacer(Modifier.height(16.dp))
            members.forEachIndexed { index, member ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemberAvatar(member = member, size = 40.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(member.name, fontWeight = FontWeight.Bold, color = DarkText, fontSize = 15.sp)
                        Text("Miembro del hogar", fontSize = 12.sp, color = MediumDarkGray)
                    }
                }
                if (index < members.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = SilverGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}