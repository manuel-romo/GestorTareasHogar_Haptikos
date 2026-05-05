package haptikos.gestortareashogar_haptikos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricAuthBottomSheet(
    title: String,
    description: String,
    warningText: String? = null,
    confirmButtonText: String = "Autenticar",
    confirmButtonColor: Color = Color(0xFFFF4B4B),
    onDismissRequest: () -> Unit,
    onAuthenticateClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón Cerrar Superior Derecho
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .clickable { onDismissRequest() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cross),
                        contentDescription = "Cerrar",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Icono de Huella Digital
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fingerprint),
                    contentDescription = "Huella",
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Textos Dinámicos
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Caja de Advertencia
            if (warningText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF0F0), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_danger),
                        contentDescription = "Advertencia",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = warningText,
                        color = Color(0xFFE53935),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Botones
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.weight(1f).height(52.dp).clickable { onDismissRequest() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Cancelar", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f).height(52.dp).clickable { onAuthenticateClick() },
                    shape = RoundedCornerShape(16.dp),
                    color = confirmButtonColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_fingerprint), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(confirmButtonText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}