package haptikos.gestortareashogar_haptikos.ui.screens.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun LogInHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_email),
                contentDescription = "Icono de correo",
                modifier = Modifier.padding(16.dp).size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Haptikos",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "Tu hogar, más organizado \uD83C\uDFE0",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
        )
    }
}