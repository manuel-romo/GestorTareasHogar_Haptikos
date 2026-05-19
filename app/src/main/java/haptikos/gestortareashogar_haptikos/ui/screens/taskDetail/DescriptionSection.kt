package haptikos.gestortareashogar_haptikos.ui.screens.taskDetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.ui.theme.DarkText
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray

@Composable
fun DescriptionSection(description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, SilverGray)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Descripción completa", fontWeight = FontWeight.Bold, color = MediumDarkGray, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            if (description.isNotBlank()) {
                Text(description, color = DarkText, fontSize = 14.sp, lineHeight = 20.sp)
            } else {
                Text("Sin descripción adicional.", color = SilverGray, fontSize = 14.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}