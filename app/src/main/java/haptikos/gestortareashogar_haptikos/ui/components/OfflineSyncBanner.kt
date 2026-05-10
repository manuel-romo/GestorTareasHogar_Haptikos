package haptikos.gestortareashogar_haptikos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
fun OfflineSyncBanner(
    isOffline: Boolean,
    hasPendingSyncs: Boolean
) {

    AnimatedVisibility(
        visible = isOffline && hasPendingSyncs,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF44336))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_cloud_off),
                contentDescription = "Sin conexión",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = "Modo sin conexión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Tus datos están guardados localmente. Se sincronizarán cuando vuelva el internet.",
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}