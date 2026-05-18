package haptikos.gestortareashogar_haptikos.ui.screens.userEdition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatCard(number: String, label: String) {
    Column(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}