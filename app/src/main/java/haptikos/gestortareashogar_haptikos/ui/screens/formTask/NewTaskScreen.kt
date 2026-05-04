package haptikos.gestortareashogar_haptikos.ui.screens.formTask

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskWithDetails
import haptikos.gestortareashogar_haptikos.ui.components.GenericMultiSelectionBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.GenericSelectionBottomSheet
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode
import haptikos.gestortareashogar_haptikos.ui.enums.TurnMode
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel

@Composable
fun NewTaskScreen(
    taskId: Int? = null,
    roomViewModel: RoomViewModel,
    taskViewModel: TaskViewModel,
    memberViewModel: MemberViewModel,
    onReturn:() -> Unit
){
    val roomList by roomViewModel.rooms.collectAsState()
    val memberList by memberViewModel.members.collectAsState()


    // Se busca si la tarea existe en la BD.
    var taskToEdit by remember { mutableStateOf<TaskWithDetails?>(null) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            taskToEdit = taskViewModel.getByIdNew(taskId)
        }
    }

    NewTaskContent(
        roomList = roomList,
        memberList = memberList,
        taskToEdit = taskToEdit,
        onReturn = onReturn,
        onSaveTask = { name, desc, room, day, recurrence, priority, workMode, orderedMembers ->

            if (name.isNotBlank()) {
                val task = TaskEntityNew(
                    // Si es edición, se conserva el ID que tiene la tarea.
                    id = taskToEdit?.task?.id ?: 0,
                    title = name,
                    description = desc,
                    roomId = room?.id,
                    points = priority.points,
                    priority = priority,
                    suggestedDay = day,
                    recurrence = recurrence,
                    workMode = workMode
                )

                val memberIds = orderedMembers.map { it.id }

                if (taskToEdit == null) {
                    taskViewModel.addTaskNew(task,memberIds)
                } else {
                    taskViewModel.updateTaskNew(task, memberIds)
                }
                onReturn()
            }
        }
    )
}

// Colores
val OrangeMain = Color(0xFFFF8A00)
val GrayText = Color(0xFF8E8E93)
val LightBg = Color(0xFFF9F9F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskContent(
    roomList: List<RoomEntityNew>,
    memberList: List<MemberEntityNew>,
    taskToEdit: TaskWithDetails? = null,
    onReturn: () -> Unit,
    onSaveTask: (
        String,
        String,
        RoomEntityNew?,
        SuggestedDay,
        RecurrenceType,
        PriorityLevel,
        WorkMode,
        List<MemberEntityNew>
    ) -> Unit
) {
    // Estados inicializados directamente con los datos de taskToEdit (si existe)
    var taskName by remember(taskToEdit) { mutableStateOf(taskToEdit?.task?.title ?: "") }
    var taskDescription by remember(taskToEdit) { mutableStateOf(taskToEdit?.task?.description ?: "") }
    var isRoomScope by remember(taskToEdit) { mutableStateOf(taskToEdit?.room != null) }
    var selectedPriority by remember(taskToEdit) { mutableStateOf(taskToEdit?.task?.priority ?: PriorityLevel.MEDIA) }
    var selectedDay by remember(taskToEdit) { mutableStateOf(taskToEdit?.task?.suggestedDay ?: SuggestedDay.LUNES) }
    var selectedRecurrence by remember(taskToEdit) { mutableStateOf(taskToEdit?.task?.recurrence ?: RecurrenceType.DIARIO) }
    var selectedRoom by remember(taskToEdit) { mutableStateOf(taskToEdit?.room) }
    var selectedMembers by remember(taskToEdit) { mutableStateOf(taskToEdit?.members?.toSet() ?: emptySet()) }

    // Estados de equipo
    var selectedWorkMode by remember(taskToEdit) { mutableStateOf(taskToEdit?.task?.workMode ?: WorkMode.TEAM) }
    var selectedTurnMode by remember(taskToEdit) { mutableStateOf(TurnMode.RANDOM) }

    val orderedTurns = remember { mutableStateListOf<MemberEntityNew>() }

    // Estados de menús
    var showDaySelector by remember { mutableStateOf(false) }
    var showRecurrenceSelector by remember { mutableStateOf(false) }
    var showRoomSelector by remember { mutableStateOf(false) }
    var showMemberSelector by remember { mutableStateOf(false) }

    // Actualiza lista de turnos automáticamente cuando cambian los miembros
    LaunchedEffect(selectedMembers) {
        orderedTurns.clear()
        orderedTurns.addAll(selectedMembers)
    }

    // Estructura principal
    Scaffold(
        topBar = { TaskTopAppBar(isEditing = taskToEdit != null, onReturn = onReturn) },
        bottomBar = {
            TaskBottomBar(
                isEditing = taskToEdit != null,
                onReturn = onReturn,
                onSaveClick = {
                    onSaveTask(
                        taskName,
                        taskDescription,
                        selectedRoom,
                        selectedDay,
                        selectedRecurrence,
                        selectedPriority,
                        selectedWorkMode,
                        orderedTurns.toList()
                    )
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {

            BasicInfoSection(taskName, { taskName = it }, taskDescription, { taskDescription = it })

            ScopeSection(isRoomScope, { isRoomScope = it }, selectedRoom) { showRoomSelector = true }

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
                    onShuffleTurns = { orderedTurns.shuffle() },
                    onMoveTurn = { fromIndex, toIndex ->
                        val item = orderedTurns.removeAt(fromIndex)
                        orderedTurns.add(toIndex, item)
                    }
                )
            }
        }
    }

    // Menús emergentes
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
fun TaskTopAppBar(
    isEditing: Boolean = false,
    onReturn:() -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditing) "Editar tarea" else "Nueva tarea",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onReturn() },
                modifier = Modifier
                    .padding(start = 30.dp, end = 20.dp)
                    .size(20.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Atrás",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OrangeMain,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}
@Composable
fun TaskBottomBar(
    isEditing: Boolean = false,
    onReturn:() -> Unit,
    onSaveClick:() -> Unit
) {
    Surface(color = Color.White, shadowElevation = 8.dp, modifier = Modifier.navigationBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { onReturn() }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F7)), shape = RoundedCornerShape(12.dp)) {
                Text("Cancelar", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(onClick = { onSaveClick() }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = OrangeMain), shape = RoundedCornerShape(12.dp)) {
                Text(if (isEditing) "Guardar cambios" else "Crear tarea", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Secciones de formulario
@Composable
fun BasicInfoSection(name: String, onNameChange: (String) -> Unit, desc: String, onDescChange: (String) -> Unit) {
    FormLabel(text = "NOMBRE DE LA TAREA *", iconRes = R.drawable.ic_t_text)
    OutlinedTextField(
        value = name, onValueChange = onNameChange, placeholder = { Text("Nombre de la tarea", color = Color.LightGray) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangeMain, unfocusedBorderColor = Color(0xFFE5E5EA), unfocusedContainerColor = LightBg, focusedContainerColor = Color.White), singleLine = true
    )

    FormLabel(text = "DESCRIPCIÓN BREVE", iconRes = R.drawable.ic_lines)
    OutlinedTextField(
        value = desc, onValueChange = onDescChange, placeholder = { Text("Descripción de la tarea", color = Color.LightGray) },
        modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 24.dp), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangeMain, unfocusedBorderColor = Color(0xFFE5E5EA), unfocusedContainerColor = LightBg, focusedContainerColor = Color.White), maxLines = 4
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
        Text(text = if (isRoomScope) "La tarea pertenece a una habitación concreta." else "La tarea aplica a todo el hogar, sin habitación específica.", color = GrayText, fontSize = 12.sp)
    }

    if (isRoomScope) {
        Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).clickable { onOpenRoomMenu() }, shape = RoundedCornerShape(16.dp), color = LightBg, border = BorderStroke(1.dp, Color(0xFFE5E5EA))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                    if (selectedRoom != null) Text(text = selectedRoom.icon, fontSize = 20.sp)
                    else Icon(painterResource(id = R.drawable.ic_location), contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Habitación", color = GrayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = selectedRoom?.name ?: "Selecciona una habitación", color = if (selectedRoom != null) Color.Black else Color.LightGray, fontSize = 14.sp, fontWeight = if (selectedRoom != null) FontWeight.Bold else FontWeight.Normal)
                }
                Icon(painterResource(id = R.drawable.ic_arrow_right), contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ScheduleSection(selectedDay: SuggestedDay, selectedRecurrence: RecurrenceType, onOpenDayMenu: () -> Unit, onOpenRecurrenceMenu: () -> Unit) {
    SectionTitle("PROGRAMACIÓN")
    Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), shape = RoundedCornerShape(16.dp), color = LightBg, border = BorderStroke(1.dp, Color(0xFFF0F0F0))) {
        Column {
            ScheduleOptionRow(iconBgColor = Color(0xFFFFF0E0), iconColor = Color(0xFFFF9800), iconRes = R.drawable.ic_calendar, title = "Día sugerido", value = selectedDay.displayName, valuePrefix = "${selectedDay.icon} ", onClick = onOpenDayMenu)
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            ScheduleOptionRow(iconBgColor = Color(0xFFF3E5F5), iconColor = Color(0xFF9C27B0), iconRes = R.drawable.ic_recurrency, title = "Recurrencia", value = selectedRecurrence.displayName, valuePrefix = "${selectedRecurrence.icon} ", onClick = onOpenRecurrenceMenu)
        }
    }
}

@Composable
fun PrioritySection(selectedPriority: PriorityLevel, onPriorityChange: (PriorityLevel) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        SectionTitle("PRIORIDAD", paddingBottom = 0.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(id = R.drawable.ic_star), contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Afecta los puntos ganados", color = GrayText, fontSize = 12.sp)
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PriorityLevel.values().forEach { priority ->
            PriorityCard(priority = priority, isSelected = selectedPriority == priority, onClick = { onPriorityChange(priority) }, modifier = Modifier.weight(1f))
        }
    }
}


// Otras funciones de utilidad
@Composable
fun SectionTitle(title: String, paddingBottom: androidx.compose.ui.unit.Dp = 12.dp) {
    Text(text = title, color = GrayText, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = paddingBottom))
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
            tint = Color(0xFF8E8E93)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFF8E8E93),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}


@Preview(showBackground = true)
@Composable
fun NewTaskScreenPreview_Agregar() {
    val rooms = listOf(
        RoomEntityNew(id = 1, name = "Cocina", icon = "🍳", colorHex = "#FF5252"),
        RoomEntityNew(id = 2, name = "Sala", icon = "🛋️", colorHex = "#448AFF"),
        RoomEntityNew(id = 3, name = "Baños", icon = "🚿", colorHex = "#4CAF50")
    )

    val members = listOf(
        MemberEntityNew(id = 1, name = "María", lastName = "López", colorHex = "#F014A8", MemberRole.MEMBER),
        MemberEntityNew(id = 2, name = "Juan", lastName = "Pérez", colorHex = "#2979FF", MemberRole.MEMBER)
    )

    GestorTareasHogar_HaptikosTheme {
        NewTaskContent(
            roomList = rooms,
            memberList = members,
            taskToEdit = null,
            onReturn = {},
            onSaveTask = { _, _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewTaskScreenPreview_Editar() {
    val rooms = listOf(
        RoomEntityNew(id = 1, name = "Cocina", icon = "🍳", colorHex = "#FF5252"),
        RoomEntityNew(id = 2, name = "Sala", icon = "🛋️", colorHex = "#448AFF")
    )

    val members = listOf(
        MemberEntityNew(id = 1, name = "María", lastName = "López", colorHex = "#F014A8", role = MemberRole.MEMBER),
        MemberEntityNew(id = 2, name = "Juan", lastName = "Pérez", colorHex = "#2979FF", role = MemberRole.MEMBER)
    )

    val tareaBase = TaskEntityNew(
        id = 10,
        title = "Limpiar la cocina a fondo",
        description = "Usar desengrasante en la estufa",
        roomId = 1,
        points = 20,
        priority = PriorityLevel.ALTA,
        suggestedDay = SuggestedDay.SABADO,
        recurrence = RecurrenceType.SEMANAL,
        workMode = WorkMode.TEAM
    )

    val tareaConDetalles = TaskWithDetails(
        task = tareaBase,
        room = rooms[0],
        members = members
    )

    GestorTareasHogar_HaptikosTheme {
        NewTaskContent(
            roomList = rooms,
            memberList = members,
            taskToEdit = tareaConDetalles,
            onReturn = {},
            onSaveTask = { _, _, _, _, _, _, _, _ -> }
        )
    }
}


