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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    onNavigateToNewPredeterminedTask: (roomId: String) -> Unit,
    onNavigateToEditTask: (taskId: String) -> Unit,
    onNavigateToNewTask: (roomId: String?) -> Unit,
    onLeaveHome: () -> Unit
) {
    val selectedHome by homeViewModel.selectedHome.collectAsState()
    val showSuccessFeedback by homeViewModel.showSuccessFeedback.collectAsState()
    val isCreator by homeViewModel.isCurrentUserCreator.collectAsState()
    val canEditTasks by homeViewModel.canCurrentUserEditTasks.collectAsState()

    val homeId = selectedHome?.id ?: ""

    val members by remember(homeId) {
        memberViewModel.getMembersForHomeWithUserContext(homeId)
    }.collectAsState(initial = emptyList())

    // Feedback al eliminar el hogar
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

    val home = selectedHome
    if (home == null && !showSuccessFeedback) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (home != null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->

            val scrollState = rememberScrollState()
            val isCollapsed by remember {
                derivedStateOf { scrollState.value > 50 }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                val context = LocalContext.current
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                HomeConfigurationHeader(
                    homeName = home.name,
                    inviteCode = home.inviteCode ?: "Pendiente...",
                    isCollapsed = isCollapsed,
                    onBack = onBack,
                    onCopyCode = {
                        val clip = ClipData.newPlainText("Código de invitación", home.inviteCode)
                        clipboard.setPrimaryClip(clip)
                    },
                    onRegenerateCode = if (isCreator) {
                        { homeViewModel.regenerateInviteCode { } }
                    } else null
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isCreator) {
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
                    }

                    MembersSection(
                        memberViewModel = memberViewModel,
                        members = members,
                        homeId = home.id,
                        isCreator = isCreator
                    )

                    // Sección de habitaciones y tareas
                    RoomsTasksSection(
                        homeId = home.id,
                        roomViewModel = roomViewModel,
                        taskViewModel = taskViewModel,
                        onNavigateToEditPredeterminedTask = onNavigateToEditPredeterminedTask,
                        onNavigateToNewPredeterminedTask  = onNavigateToNewPredeterminedTask,
                        onNavigateToEditTask = onNavigateToEditTask,
                        onNavigateToNewTask = onNavigateToNewTask,
                        isCreator = isCreator,
                        canEditTasks = canEditTasks
                    )

                    if (isCreator) {
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

                        DangerZoneSection(
                            homeViewModel = homeViewModel,
                            onHomeDeleted = onBack
                        )
                    } else {
                        LeaveHomeSection(
                            onLeaveClick = {
                                homeViewModel.leaveCurrentHome(onSuccess = onLeaveHome)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}