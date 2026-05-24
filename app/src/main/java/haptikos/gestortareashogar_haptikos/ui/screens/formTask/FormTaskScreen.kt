package haptikos.gestortareashogar_haptikos.ui.screens.formTask

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskWithDetails
import haptikos.gestortareashogar_haptikos.ui.components.GenericMultiSelectionBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.GenericSelectionBottomSheet
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode
import haptikos.gestortareashogar_haptikos.ui.enums.TurnMode
import haptikos.gestortareashogar_haptikos.ui.theme.BlackGray
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.LightGray
import haptikos.gestortareashogar_haptikos.ui.theme.LightPurple
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.Orange
import haptikos.gestortareashogar_haptikos.ui.theme.Purple
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray
import haptikos.gestortareashogar_haptikos.ui.theme.White
import haptikos.gestortareashogar_haptikos.ui.theme.YellowGreen
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel
import kotlinx.coroutines.flow.flowOf
import java.util.UUID


@Composable
fun FormTaskScreen(
    taskId: String? = null,
    roomId: String? = null,
    isPredetermined: Boolean = false,
    roomViewModel: RoomViewModel,
    taskViewModel: TaskViewModel,
    memberViewModel: MemberViewModel,
    homeViewModel: HomeViewModel,
    onReturn:() -> Unit
){
    val selectedHome by homeViewModel.selectedHome.collectAsState()
    val roomList by roomViewModel.rooms.collectAsState()

    val memberList by remember(selectedHome?.id) {
        selectedHome?.let { memberViewModel.getMembersForHome(it.id) }
            ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    var taskToEdit by remember { mutableStateOf<TaskWithDetails?>(null) }
    var preselectedRoom by remember { mutableStateOf<RoomEntityNew?>(null) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            taskToEdit = taskViewModel.getByIdNew(taskId)
        }
    }

    LaunchedEffect(roomId, roomList) {
        if (roomId != null && roomList.isNotEmpty()) {
            preselectedRoom = roomList.find { it.id == roomId }
        }
    }

    // No renderiza el formulario hasta que la habitación esté resuelta
    if (isPredetermined && roomId != null && preselectedRoom == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        FormatTaskContent(
            roomList = roomList,
            memberList = memberList,
            taskToEdit = taskToEdit,
            preselectedRoom = preselectedRoom,
            isPredetermined = isPredetermined,
            onReturn = onReturn,
            onSaveTask = { name, desc, room, day, recurrence, priority, workMode, orderedMembers ->
                if (name.isNotBlank()) {
                    val task = TaskEntityNew(
                        id = taskToEdit?.task?.id ?: UUID.randomUUID().toString(),
                        title = name,
                        description = desc,
                        roomId = room?.id,
                        homeId = selectedHome?.id ?: "",
                        points = priority.points,
                        priority = priority,
                        suggestedDay = day,
                        recurrence = recurrence,
                        workMode = workMode,
                        isPredetermined = isPredetermined
                    )
                    val memberIds = orderedMembers.map { it.id }
                    if (taskToEdit == null) {
                        taskViewModel.addTaskNew(task, memberIds)
                    } else {
                        taskViewModel.updateTaskNew(task, memberIds)
                    }
                    onReturn()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatTaskContent(
    roomList: List<RoomEntityNew>,
    memberList: List<MemberEntityNew>,
    taskToEdit: TaskWithDetails? = null,
    preselectedRoom: RoomEntityNew? = null,
    isPredetermined: Boolean = false,
    onReturn: () -> Unit,
    onSaveTask: (String, String, RoomEntityNew?, SuggestedDay, RecurrenceType, PriorityLevel, WorkMode, List<MemberEntityNew>) -> Unit
) {
    // Valores por defecto, se sobreescriben si carga una tarea a editar
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(PriorityLevel.MEDIA) }
    var selectedDay by remember { mutableStateOf(SuggestedDay.LUNES) }
    var selectedRecurrence by remember { mutableStateOf(RecurrenceType.DIARIO) }
    var selectedMembers by remember { mutableStateOf<Set<MemberEntityNew>>(emptySet()) }
    var selectedRoom by remember { mutableStateOf<RoomEntityNew?>(preselectedRoom) }
    var isRoomScope by remember { mutableStateOf(preselectedRoom != null) }
    var selectedWorkMode by remember { mutableStateOf(WorkMode.TEAM) }
    var selectedTurnMode by remember { mutableStateOf(TurnMode.RANDOM) }

    val orderedTurns = remember { mutableStateListOf<MemberEntityNew>() }

    LaunchedEffect(preselectedRoom) {
        if (taskToEdit == null && preselectedRoom != null) {
            selectedRoom = preselectedRoom
            isRoomScope = true
            Log.d("FORM_ROOM", "selectedRoom aplicado: ${selectedRoom?.id}")
        }
    }

    LaunchedEffect(taskToEdit) {
        taskToEdit?.let { task ->
            Log.d("FORM_TASK", "room=${task.room?.id} roomId=${task.task.roomId}")
            taskName = task.task.title
            taskDescription = task.task.description
            selectedPriority = task.task.priority
            selectedDay = task.task.suggestedDay
            selectedRecurrence = task.task.recurrence
            selectedWorkMode = task.task.workMode
            selectedMembers = task.members.toSet()
            selectedRoom = task.room ?: preselectedRoom
            isRoomScope = task.room != null || preselectedRoom != null
            orderedTurns.clear()
            orderedTurns.addAll(task.members)
        }
    }

    LaunchedEffect(selectedMembers) {
        if (taskToEdit == null) {
            orderedTurns.clear()
            orderedTurns.addAll(selectedMembers)
        }
    }

    var showDaySelector by remember { mutableStateOf(false) }
    var showRecurrenceSelector by remember { mutableStateOf(false) }
    var showRoomSelector by remember { mutableStateOf(false) }
    var showMemberSelector by remember { mutableStateOf(false) }

    val isFormValid = remember(taskName, isRoomScope, selectedRoom, selectedMembers) {
        val hasName = taskName.isNotBlank()
        val hasValidRoom = if (isRoomScope) selectedRoom != null else true
        val hasMembers = selectedMembers.isNotEmpty()
        hasName && hasValidRoom && hasMembers
    }

    Log.d("FORM_VALID", "taskName='$taskName' isRoomScope=$isRoomScope selectedRoom=${selectedRoom?.id} members=${selectedMembers.size} isFormValid=$isFormValid")
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TaskTopAppBar(
                isEditing = taskToEdit != null,
                roomInfo = if (isPredetermined) selectedRoom else null,
                onReturn = onReturn
            )
        },
        bottomBar = {
            TaskBottomBar(
                isEditing = taskToEdit != null,
                isSaveEnabled = isFormValid,
                onReturn = onReturn,
                onSaveClick = {
                    onSaveTask(taskName, taskDescription, selectedRoom, selectedDay, selectedRecurrence, selectedPriority, selectedWorkMode, orderedTurns.toList())
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            BasicInfoSection(taskName, { taskName = it }, taskDescription, { taskDescription = it })

            if (!isPredetermined) {
                ScopeSection(isRoomScope, { isRoomScope = it }, selectedRoom) { showRoomSelector = true }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            ScheduleSection(selectedDay, selectedRecurrence, { showDaySelector = true }) { showRecurrenceSelector = true }

            PrioritySection(selectedPriority) { selectedPriority = it }

            AssignmentSection(selectedMembers) { showMemberSelector = true }

            if (selectedMembers.size >= 2) {
                WorkModeSection(
                    selectedWorkMode = selectedWorkMode,
                    onWorkModeChange = { selectedWorkMode = it },
                    selectedTurnMode = selectedTurnMode,
                    onTurnModeChange = { selectedTurnMode = it },
                    orderedTurns = orderedTurns,
                    selectedRecurrence = selectedRecurrence,
                    onShuffleTurns = { orderedTurns.shuffle() },
                    onMoveTurn = { fromIndex, toIndex ->
                        val item = orderedTurns.removeAt(fromIndex)
                        orderedTurns.add(toIndex, item)
                    }
                )
            }
        }
    }

    TaskBottomSheets(
        showDaySelector = showDaySelector,
        onDismissDay = { showDaySelector = false },
        selectedDay = selectedDay,
        onDaySelected = { selectedDay = it },
        showRecurrenceSelector = showRecurrenceSelector,
        onDismissRecurrence = { showRecurrenceSelector = false },
        selectedRecurrence = selectedRecurrence,
        onRecurrenceSelected = { selectedRecurrence = it },
        showRoomSelector = showRoomSelector,
        onDismissRoom = { showRoomSelector = false },
        roomList = roomList,
        selectedRoom = selectedRoom,
        onRoomSelected = { selectedRoom = it },
        showMemberSelector = showMemberSelector,
        onDismissMember = { showMemberSelector = false },
        memberList = memberList,
        selectedMembers = selectedMembers,
        onMemberToggled = { member ->
            selectedMembers = if (selectedMembers.contains(member)) selectedMembers - member else selectedMembers + member
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTopAppBar(isEditing: Boolean = false, roomInfo: RoomEntityNew? = null, onReturn: () -> Unit) {
    if (roomInfo != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BrightOrange, Orange)
                    )
                )
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                IconButton(
                    onClick = { onReturn() },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .background(White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_back),
                        "Atrás",
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = if (isEditing) "Editar tarea" else "Nueva tarea",
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = White.copy(alpha = 0.2f)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = roomInfo.icon, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Habitación", color = White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text(roomInfo.name, color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Surface(color = White.copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp)) {
                        Text("Predeterminada", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(colors = listOf(BrightOrange, Orange))
                )
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Flecha a la izquierda
            IconButton(
                onClick = { onReturn() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .background(White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_back),
                    "Atrás",
                    tint = White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = if (isEditing) "Editar tarea" else "Nueva tarea",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun TaskBottomBar(
    isEditing: Boolean = false,
    onReturn:() -> Unit,
    onSaveClick:() -> Unit,
    isSaveEnabled: Boolean = true
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onReturn() },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onSaveClick() },
                enabled = isSaveEnabled,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Guardar" else "Crear tarea",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BasicInfoSection(name: String, onNameChange: (String) -> Unit, desc: String, onDescChange: (String) -> Unit) {
    FormLabel(text = "NOMBRE DE LA TAREA *", iconRes = R.drawable.ic_t_text)
    OutlinedTextField(
        value = name, onValueChange = onNameChange,
        placeholder = { Text("Nombre de la tarea", color = MediumDarkGray) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrightOrange,
            unfocusedBorderColor = SilverGray,
            focusedContainerColor = White,
            unfocusedContainerColor = LightGray
        ),
        singleLine = true
    )

    FormLabel(text = "DESCRIPCIÓN BREVE", iconRes = R.drawable.ic_lines)
    OutlinedTextField(
        value = desc, onValueChange = onDescChange,
        placeholder = { Text("Descripción de la tarea", color = MediumDarkGray) },
        modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrightOrange,
            unfocusedBorderColor = SilverGray,
            focusedContainerColor = White,
            unfocusedContainerColor = LightGray
        ),
        maxLines = 4
    )
}

@Composable
fun ScopeSection(
    isRoomScope: Boolean,
    onScopeChange: (Boolean) -> Unit,
    selectedRoom: RoomEntityNew?,
    onOpenRoomMenu: () -> Unit) {

    SectionTitle("ALCANCE")
    ScopeSelector(isRoomScope = isRoomScope, onScopeChange = onScopeChange)

    Row(modifier = Modifier.padding(top = 12.dp, bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = if (isRoomScope) "🚪" else "🏠", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isRoomScope) "La tarea pertenece a una habitación concreta." else "La tarea aplica a todo el hogar, sin habitación específica.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }

    if (isRoomScope) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).clickable { onOpenRoomMenu() },
            shape = RoundedCornerShape(16.dp),
            color = LightGray,
            border = BorderStroke(1.dp, SilverGray)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedRoom != null) Text(text = selectedRoom.icon, fontSize = 20.sp)
                    else Icon(painterResource(id = R.drawable.ic_location), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Habitación", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = selectedRoom?.name ?: "Selecciona una habitación",
                        color = if (selectedRoom != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp,
                        fontWeight = if (selectedRoom != null) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Icon(painterResource(id = R.drawable.ic_arrow_right), contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ScheduleSection(
    selectedDay: SuggestedDay,
    selectedRecurrence: RecurrenceType,
    onOpenDayMenu: () -> Unit,
    onOpenRecurrenceMenu: () -> Unit
) {
    SectionTitle("PROGRAMACIÓN")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightGray,
        border = BorderStroke(1.dp, SilverGray)
    ) {
        Column {
            if (selectedRecurrence == RecurrenceType.SEMANAL) {
                ScheduleOptionRow(
                    iconBgColor = Color(0xFFFFF0E0), iconColor = BrightOrange,
                    iconRes = R.drawable.ic_calendar, title = "Día sugerido",
                    value = selectedDay.displayName, valuePrefix = "${selectedDay.icon} ",
                    onClick = onOpenDayMenu
                )
                HorizontalDivider(
                    color = SilverGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            ScheduleOptionRow(
                iconBgColor = LightPurple, iconColor = Purple,
                iconRes = R.drawable.ic_recurrency, title = "Recurrencia",
                value = selectedRecurrence.displayName, valuePrefix = "${selectedRecurrence.icon} ",
                onClick = onOpenRecurrenceMenu
            )
        }
    }

    val scheduleHint = when (selectedRecurrence) {
        RecurrenceType.DIARIO -> "📅 Se generará todos los días"
        RecurrenceType.SEMANAL -> "📅 Se generará cada ${selectedDay.displayName}"
        RecurrenceType.QUINCENAL -> "📅 Se generará cada 15 días desde su creación"
        RecurrenceType.MENSUAL -> "📅 Se generará el mismo día de cada mes"
    }

    Text(
        text = scheduleHint,
        color = MediumDarkGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
    )
}

@Composable
fun SectionTitle(title: String, paddingBottom: androidx.compose.ui.unit.Dp = 12.dp) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = paddingBottom)
    )
}

@Composable
fun FormLabel(text: String, iconRes: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}



@Composable
fun PrioritySection(selectedPriority: PriorityLevel, onPriorityChange: (PriorityLevel) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle("PRIORIDAD", paddingBottom = 0.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(id = R.drawable.ic_star), null, tint = YellowGreen, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Afecta los puntos ganados", color = MediumDarkGray, fontSize = 12.sp)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PriorityLevel.values().forEach { priority ->
            PriorityCard(
                priority = priority,
                isSelected = selectedPriority == priority,
                onClick = { onPriorityChange(priority) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}



@Composable
fun TaskBottomSheets(
    showDaySelector: Boolean,
    onDismissDay: () -> Unit,
    selectedDay: SuggestedDay,
    onDaySelected: (SuggestedDay) -> Unit,
    showRecurrenceSelector: Boolean,
    onDismissRecurrence: () -> Unit,
    selectedRecurrence: RecurrenceType,
    onRecurrenceSelected: (RecurrenceType) -> Unit,
    showRoomSelector: Boolean,
    onDismissRoom: () -> Unit,
    roomList: List<RoomEntityNew>,
    selectedRoom: RoomEntityNew?,
    onRoomSelected: (RoomEntityNew) -> Unit,
    showMemberSelector: Boolean,
    onDismissMember: () -> Unit,
    memberList: List<MemberEntityNew>,
    selectedMembers: Set<MemberEntityNew>,
    onMemberToggled: (MemberEntityNew) -> Unit
) {
    if (showDaySelector) {
        GenericSelectionBottomSheet(
            showSheet = showDaySelector,
            onDismissRequest = onDismissDay,
            title = "Día sugerido",
            description = "El día que se propondrá por defecto al asignar esta tarea.",
            items = SuggestedDay.values().toList(),
            selectedItem = selectedDay,
            onItemSelected = onDaySelected,
            itemIcon = { it.icon },
            itemText = { it.displayName })
    }
    if (showRecurrenceSelector) {
        GenericSelectionBottomSheet(
            showSheet = showRecurrenceSelector,
            onDismissRequest = onDismissRecurrence,
            title = "Recurrencia",
            description = "¿Con qué frecuencia se debe repetir esta tarea?",
            items = RecurrenceType.values().toList(),
            selectedItem = selectedRecurrence,
            onItemSelected = onRecurrenceSelected,
            itemIcon = { it.icon },
            itemText = { it.displayName })
    }
    if (showRoomSelector){
        GenericSelectionBottomSheet(
            showSheet = showRoomSelector,
            onDismissRequest = onDismissRoom,
            title = "Habitación",
            description = "La habitación a la que corresponde esta tarea",
            items = roomList,
            selectedItem = selectedRoom,
            onItemSelected = onRoomSelected,
            itemIcon = { it.icon },
            itemText = { it.name })
    }
    if (showMemberSelector) {
        GenericMultiSelectionBottomSheet(
            showSheet = showMemberSelector,
            onDismissRequest = onDismissMember,
            title = "Miembros",
            description = "Selecciona uno o más miembros para esta tarea",
            items = memberList,
            selectedItems = selectedMembers,
            onItemToggled = onMemberToggled,
            onConfirm = onDismissMember,
            itemBgColorHex = { it.colorHex},
            itemText = { it.name }
        )
    }
}
