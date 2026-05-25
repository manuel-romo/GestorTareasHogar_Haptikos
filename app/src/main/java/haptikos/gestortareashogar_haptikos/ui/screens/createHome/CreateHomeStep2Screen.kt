package haptikos.gestortareashogar_haptikos.ui.screens.createHome

import android.content.Intent
import android.graphics.Color.parseColor
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.identity.util.UUID
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.helpers.UserSuggestion
import haptikos.gestortareashogar_haptikos.ui.components.MemberAvatar
import haptikos.gestortareashogar_haptikos.ui.enums.InviteType
import haptikos.gestortareashogar_haptikos.utils.parseHexColor
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel


data class InvitedUser(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val type: InviteType,
    val avatarInitials: String = "",
    val avatarColor: Color = Color.LightGray,
    val originalUser: UserSuggestion? = null,
    val userId: String? = null
)

fun UserSuggestion.toInvitedUser(): InvitedUser {
    return InvitedUser(
        id = this.id,
        title = this.fullName,
        subtitle = this.username,
        type = InviteType.APP,
        avatarInitials = this.fullName.take(1).uppercase(),
        avatarColor = parseHexColor(this.colorHex),
        originalUser = this
    )
}

@Composable
fun CreateHomeStep2Screen(
    homeName: String,
    homeDesc: String,
    colorHex: String,
    isPrivate: Boolean,
    userFullName: String,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit,
    onSuccess: (String, String?) -> Unit
) {
    val allMembers by homeViewModel.suggestedUsers.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val inviteEmailState by homeViewModel.inviteEmailState.collectAsState()

    var invitedUsers by remember { mutableStateOf(listOf<InvitedUser>()) }

    // Filtrar sugerencias por búsqueda localmente
    val filteredMembers = remember(allMembers, searchQuery) {
        if (searchQuery.isBlank()) allMembers
        else allMembers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.lastName.contains(searchQuery, ignoreCase = true)
        }
    }

    // Quitar los ya invitados
    val availableSuggestions = remember(filteredMembers, invitedUsers) {
        filteredMembers.filter { member ->
            invitedUsers.none { invited -> invited.id == member.id }
        }
    }

    CreateHomeStep2Content(
        homeName = if (homeName.isNotBlank()) homeName else "Mi Hogar",
        availableSuggestions = availableSuggestions,
        searchQuery = searchQuery,
        onSearchChanged = { homeViewModel.onSearchQueryChanged(it) },
        invitedUsers = invitedUsers,
        inviteEmailState = inviteEmailState,
        onAddMember = { member ->
            val invited = InvitedUser(
                id = member.id,
                title = "${member.name} ${member.lastName}".trim(),
                subtitle = member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                type = InviteType.APP,
                avatarInitials = member.name.take(1).uppercase(),
                avatarColor = try {
                    Color(parseColor(member.colorHex))
                } catch (e: Exception) { Color.Gray },
                userId = member.userId
            )
            if (invitedUsers.none { it.id == invited.id }) {
                invitedUsers = invitedUsers + invited
            }
        },
        onRemoveInvite = { invitedUsers = invitedUsers - it },
        onSendEmail = { email ->
            // El correo sea agrega a la lista de invitados
            val emailInvited = InvitedUser(
                title = email,
                subtitle = email,
                type = InviteType.EMAIL,
                avatarInitials = "@",
                avatarColor = Color(0xFFFF8A00)
            )
            if (invitedUsers.none { it.subtitle == email }) {
                invitedUsers = invitedUsers + emailInvited
            }
            // Cerrar el sheet mostrando éxito falso
            homeViewModel.resetInviteEmailState()
        },
        onResetEmailState = { homeViewModel.resetInviteEmailState() },
        onBack = onBack,
        onCreateClick = {
            val parts = userFullName.split(" ")
            val firstName = parts.firstOrNull() ?: "Usuario"
            val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""

            homeViewModel.createNewHome(
                name = homeName,
                description = homeDesc,
                isPrivate = isPrivate,
                userName = firstName,
                userLastName = lastName,
                userColor = "#$colorHex",
                invitedUsers = invitedUsers,
                onComplete = { generatedCode -> onSuccess(homeName, generatedCode) }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHomeStep2Content(
    homeName: String,
    availableSuggestions: List<MemberEntityNew>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    invitedUsers: List<InvitedUser>,
    inviteEmailState: HomeViewModel.InviteEmailState,
    onAddMember: (MemberEntityNew) -> Unit,
    onRemoveInvite: (InvitedUser) -> Unit,
    onSendEmail: (String) -> Unit,
    onResetEmailState: () -> Unit,
    onBack: () -> Unit,
    onCreateClick: () -> Unit
) {
    var showEmailSheet by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Invitar miembros",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
                            Box(Modifier.width(16.dp).height(4.dp).background(Color.White, CircleShape))
                            Box(Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_back),
                            contentDescription = "Atrás",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = { Spacer(Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF8A00))
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                val buttonText = if (invitedUsers.isNotEmpty())
                    "Crear hogar e invitar (${invitedUsers.size})"
                else "Crear hogar"

                Button(
                    onClick = onCreateClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                ) {
                    Icon(
                        painterResource(R.drawable.ic_home),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // Header con contador de invitados
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFFF8A00),
                            RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏠", fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(homeName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text(
                                    "${invitedUsers.size} miembros invitados",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                painterResource(R.drawable.ic_users),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Buscador y botones
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = { Text("Buscar por nombre...", color = Color.LightGray, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_email),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedBorderColor = Color(0xFFFF8A00),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEmailSheet = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_email),
                                contentDescription = null,
                                tint = Color(0xFFFF8A00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Invitar por correo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Invitados pendientes
            if (invitedUsers.isNotEmpty()) {
                item {
                    Text(
                        "INVITADOS (${invitedUsers.size})",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFFFF8A00),
                        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)
                    )
                }
                items(invitedUsers) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6EB)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFF0DC))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(user.avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    user.avatarInitials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(user.subtitle, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Pendiente", color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { onRemoveInvite(user) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                            ) {
                                Text("✕", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }

            // Sugerencias — miembros reales de hogares compartidos
            if (availableSuggestions.isNotEmpty()) {
                item {
                    Text(
                        "MIEMBROS DE TUS HOGARES",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                items(availableSuggestions) { member ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            MemberAvatar(member = member, size = 48.dp)

                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${member.name} ${member.lastName}".trim(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFF8A00), CircleShape)
                                    .clickable { onAddMember(member) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_user_add),
                                    contentDescription = "Añadir",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            } else if (searchQuery.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se encontraron miembros", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    LaunchedEffect(invitedUsers.size) {
        if (invitedUsers.any { it.type == InviteType.EMAIL }) {
            showEmailSheet = false
            emailInput = ""
        }
    }
    // Sheet de correo
    if (showEmailSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showEmailSheet = false
                emailInput = ""
                onResetEmailState()
            },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Invitar por correo", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        showEmailSheet = false
                        emailInput = ""
                        onResetEmailState()
                    }) {
                        Text("✕", color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Se enviará un correo con el código de invitación.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    placeholder = { Text("correo@ejemplo.com", color = Color.LightGray) },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_search),
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        focusedBorderColor = Color(0xFFFF8A00)
                    )
                )
                Spacer(Modifier.height(16.dp))

                // Feedback del estado del envío
                when (inviteEmailState) {
                    is HomeViewModel.InviteEmailState.Success -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("Correo enviado correctamente", color = Color(0xFF4CAF50), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    is HomeViewModel.InviteEmailState.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✕", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text(inviteEmailState.message, color = Color(0xFFE53935), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    else -> {}
                }

                Button(
                    onClick = { onSendEmail(emailInput) },
                    enabled = emailInput.isNotBlank() &&
                            inviteEmailState !is HomeViewModel.InviteEmailState.Loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                ) {
                    if (inviteEmailState is HomeViewModel.InviteEmailState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painterResource(R.drawable.ic_email),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar invitación", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}