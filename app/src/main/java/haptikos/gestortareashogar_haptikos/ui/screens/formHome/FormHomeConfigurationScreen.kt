package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormRoomConfigurationScreen(
    homeViewModel: HomeViewModel,
    memberViewModel: MemberViewModel,
    roomViewModel: RoomViewModel,
    taskViewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val selectedHome by homeViewModel.selectedHome.collectAsState()
    val members by memberViewModel.members.collectAsState()
    val homeName = selectedHome?.name ?: "Cargando..."
    val inviteCode = selectedHome?.inviteCode ?: "Cargando..."

    Scaffold(
        containerColor = Color(0xFFF4F5F7)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {

            HomeConfigurationHeader(
                homeName = homeName,
                inviteCode = inviteCode,
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                GeneralSection(homeName = homeName)

                HomeMembersSection(members = members)

                RoomsTasksSection(roomViewModel, taskViewModel)

                NotificationsSection()

                DangerZoneSection()

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}