package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.components.BiometricAuthBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.FeedbackBottomSheet
import haptikos.gestortareashogar_haptikos.utils.authenticateWithBiometric
import haptikos.gestortareashogar_haptikos.utils.findFragmentActivity
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel

@Composable
fun DangerZoneSection(
    homeViewModel: HomeViewModel,
    onHomeDeleted: () -> Unit
) {
    val context = LocalContext.current
    val fragmentActivity = context.findFragmentActivity()

    val selectedHome by homeViewModel.selectedHome.collectAsState()
    val isDeletingHome by homeViewModel.isDeletingHome.collectAsState()
    val showSuccessFeedback by homeViewModel.showSuccessFeedback.collectAsState()
    val actionError by homeViewModel.actionError.collectAsState()

    Column {
        SectionTitleHeader(icon = R.drawable.ic_danger, title = "ZONA DE PELIGRO")

        Card(
            onClick = { homeViewModel.initiateHomeDeletion() },
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
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trash),
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Eliminar hogar",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Elimina el hogar y todos sus datos permanentemente",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    // Diálogos
    // Autenticación
    if (isDeletingHome) {
        selectedHome?.let { home ->
            BiometricAuthBottomSheet(
                title = "Eliminar hogar",
                description = "Esta acción es crítica. Debes autenticarte para continuar.",
                warningText = "El hogar \"${home.name}\" y todos sus datos serán eliminados permanentemente para todos los miembros.",
                onDismissRequest = { homeViewModel.cancelDeletion() },
                onAuthenticateClick = {
                    if (fragmentActivity != null) {
                        authenticateWithBiometric(
                            context = fragmentActivity,
                            title = "Eliminar ${home.name}",
                            subtitle = "Confirma tu identidad para proceder",
                            onSuccess = { homeViewModel.confirmDeletion() },
                            onFailed = { homeViewModel.showBiometricError("Huella no reconocida") },
                            onError = { homeViewModel.showBiometricError(it ?: "Error desconocido") }
                        )
                    }
                }
            )
        }
    }

    // Éxito
    if (showSuccessFeedback) {
        FeedbackBottomSheet(
            title = "¡Hogar eliminado!",
            subtitle = "Los datos se han borrado correctamente.",
            isSuccess = true,
            onDismissRequest = {
                homeViewModel.dismissSuccessFeedback()
                onHomeDeleted()
            }
        )
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            homeViewModel.dismissSuccessFeedback()
            onHomeDeleted()
        }
    }

    // Error
    actionError?.let { msg ->
        FeedbackBottomSheet(
            title = "Error de autenticación",
            subtitle = msg,
            isSuccess = false,
            onDismissRequest = { homeViewModel.dismissBiometricError() }
        )
    }
}