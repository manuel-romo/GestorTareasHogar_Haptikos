package haptikos.gestortareashogar_haptikos.ui.screens.joinHome

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel.JoinHomeState

@Composable
fun JoinHomeScreen(
    viewModel: HomeViewModel,
    onBackClick: () -> Unit
) {

    LaunchedEffect(Unit) {
        viewModel.resetJoinFlow()
    }

    val state by viewModel.joinState.collectAsState()
    val code by viewModel.joinCode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        if (state !is JoinHomeState.Joining && state !is JoinHomeState.Success) {
            JoinHomeHeader(onBackClick = onBackClick)
        }

        // Transición suave entre diferentes vistas
        Crossfade(targetState = state, label = "JoinStateTransition") { currentState ->
            when (currentState) {
                is JoinHomeState.Input, is JoinHomeState.Error, is JoinHomeState.Searching -> {
                    InputCodeSection(
                        code = code,
                        onCodeChange = { viewModel.updateJoinCode(it) },
                        onSearch = { viewModel.searchHomeByCode() },
                        isLoading = currentState is JoinHomeState.Searching,
                        errorMessage = (currentState as? JoinHomeState.Error)?.message
                    )
                }
                is JoinHomeState.Found -> {
                    FoundHomeSection(
                        home = currentState.home,
                        enteredCode = code,
                        onJoinClick = { viewModel.joinFoundHome() },
                        onTryAnotherCode = { viewModel.resetJoinFlow() }
                    )
                }
                is JoinHomeState.Joining -> {
                    LoadingJoiningSection()
                }
                is JoinHomeState.Success -> {
                    SuccessJoiningSection(
                        homeName = currentState.homeName,
                        memberCount = currentState.totalMembers,
                        onFinish = onBackClick
                    )
                }
            }
        }
    }
}

@Composable
fun JoinHomeHeader(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4A68FF))
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Volver",
                    tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Unirse a un hogar",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Simulación de icono de entrada
                Text("→]", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Código de invitación", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Pide el código al creador del\nhogar e ingrésalo aquí.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}