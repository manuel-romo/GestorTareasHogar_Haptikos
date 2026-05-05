package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun DangerZoneSection() {
    Column {
        SectionTitleHeader(icon = R.drawable.ic_circle_information, title = "ZONA DE PELIGRO")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFFEBEE)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { /* Eliminar hogar */ }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_trash), null, tint = Color.Red, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Eliminar hogar", fontWeight = FontWeight.Bold, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                    Text("Elimina el hogar y todos sus datos permanentemente", color = Color(0xFFEF9A9A), style = MaterialTheme.typography.bodySmall)
                }

                Icon(painterResource(R.drawable.ic_arrow_right), null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
            }
        }
    }
}