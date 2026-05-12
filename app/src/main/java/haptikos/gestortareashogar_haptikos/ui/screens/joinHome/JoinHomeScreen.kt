package haptikos.gestortareashogar_haptikos.ui.screens.joinHome

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
                        isAlreadyMember = currentState.isAlreadyMember,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4A68FF))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .clickable { onBackClick() }
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_back), null,
                tint = Color.White, modifier = Modifier.size(18.dp))
        }

        // Título centrado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("Unirse a un hogar", color = Color.White,
                fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Ingresa el código de invitación",
                color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        }
    }
}