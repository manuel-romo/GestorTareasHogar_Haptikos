package haptikos.gestortareashogar_haptikos.ui.screens.formHome

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun LeaveHomeSection(onLeaveClick: () -> Unit) {
    Column {
        SectionTitleHeader(icon = R.drawable.ic_logout, title = "SALIR DEL HOGAR")

        Card(
            onClick = onLeaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = "Salir",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Abandonar este hogar",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Dejarás de ver las tareas y perderás tus puntos.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}