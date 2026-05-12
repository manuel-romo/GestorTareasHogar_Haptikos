package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import haptikos.gestortareashogar_haptikos.ui.components.FeedbackBottomSheet
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormHomeConfigurationScreen(
    homeViewModel: HomeViewModel,
    memberViewModel: MemberViewModel,
    roomViewModel: RoomViewModel,
    taskViewModel: TaskViewModel,
    onBack: () -> Unit,
    onNavigateToEditPredeterminedTask: (taskId: String) -> Unit,
    onNavigateToNewPredeterminedTask: (roomId: String) -> Unit
) {
    val selectedHome by homeViewModel.selectedHome.collectAsState()
    val members by memberViewModel.getMembersForHome(selectedHome?.id ?: "").collectAsState()
    val showSuccessFeedback by homeViewModel.showSuccessFeedback.collectAsState()

    if (showSuccessFeedback) {
        FeedbackBottomSheet(
            title = "¡Hogar eliminado!",
            subtitle = "Los datos se han borrado correctamente.",
            isSuccess = true,
            onDismissRequest = {
                homeViewModel.dismissSuccessFeedback()
                onBack()
            }
        )
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            homeViewModel.dismissSuccessFeedback()
            onBack()
        }
    }

    // Carga en caso de no haber hogar por algo inesperado
    val home = selectedHome
    if (home == null && !showSuccessFeedback) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF8A00))
        }
        return
    }


    if(home != null) {
        Scaffold(
            containerColor = Color(0xFFF4F5F7)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                val context = LocalContext.current
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                HomeConfigurationHeader(
                    homeName = home.name,
                    inviteCode = home.inviteCode ?: "Pendiente...",
                    onBack = onBack,
                    onCopyCode = {
                        val clip = ClipData.newPlainText("Código de invitación", home.inviteCode)
                        clipboard.setPrimaryClip(clip)
                    },
                    onRegenerateCode = {
                        homeViewModel.regenerateInviteCode { newCode ->
                            // el ViewModel ya actualiza selectedHome, la UI se recompone sola
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    GeneralSection(
                        homeName = home.name,
                        editPermission = home.editPermission,
                        onNameSave = { newName ->
                            homeViewModel.updateHome(home.copy(name = newName))
                        },
                        onPermissionChange = { permission ->
                            homeViewModel.updateHome(home.copy(editPermission = permission))
                        }
                    )

                    HomeMembersSection(members = members)

                    RoomsTasksSection(
                        homeId = home.id,
                        roomViewModel = roomViewModel,
                        taskViewModel = taskViewModel,
                        onNavigateToEditPredeterminedTask = onNavigateToEditPredeterminedTask,
                        onNavigateToNewPredeterminedTask = onNavigateToNewPredeterminedTask
                    )

                    selectedHome?.let { home ->
                        NotificationsSection(
                            home = home,
                            onUpdate = { updatedHome ->
                                homeViewModel.updateNotificationSettings(
                                    reminders = updatedHome.notifyTaskReminders,
                                    completed = updatedHome.notifyTaskCompleted,
                                    members = updatedHome.notifyNewMembers,
                                    all = updatedHome.notifyAllMembers,
                                    force = updatedHome.forceSettings
                                )
                            }
                        )
                    }

                    DangerZoneSection(
                        homeViewModel = homeViewModel,
                        onHomeDeleted = onBack
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}