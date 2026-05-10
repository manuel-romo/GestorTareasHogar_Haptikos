package haptikos.gestortareashogar_haptikos.ui.screens.createHome

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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.identity.util.UUID
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.helpers.UserSuggestion
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
    val originalUser: UserSuggestion? = null
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
    val suggestedUsers by homeViewModel.suggestedUsers.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()

    var invitedUsers by remember { mutableStateOf(listOf<InvitedUser>()) }

    CreateHomeStep2Content(
        homeName = if (homeName.isNotBlank()) homeName else "Casa Abuela",
        suggestedUsers = suggestedUsers,
        searchQuery = searchQuery,
        onSearchChanged = { homeViewModel.onSearchQueryChanged(it) },
        invitedUsers = invitedUsers,
        onAddInvite = { newInvite ->
            if (!invitedUsers.any { it.title == newInvite.title }) {
                invitedUsers = invitedUsers + newInvite
            }
        },
        onRemoveInvite = { inviteToRemove ->
            invitedUsers = invitedUsers - inviteToRemove
        },
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
                onComplete = { generatedCode ->
                    onSuccess(homeName, generatedCode)
                }
            )

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHomeStep2Content(
    homeName: String,
    suggestedUsers: List<UserSuggestion>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    invitedUsers: List<InvitedUser>,
    onAddInvite: (InvitedUser) -> Unit,
    onRemoveInvite: (InvitedUser) -> Unit,
    onBack: () -> Unit,
    onCreateClick: () -> Unit
) {

    val availableSuggestions = suggestedUsers.filter { suggested ->
        !invitedUsers.any { invited -> invited.id == suggested.id }
    }

    // Estados de tarjetas
    var showEmailSheet by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }

    var showPhoneSheet by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Invitar miembros", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Box(modifier = Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
                            Box(modifier = Modifier.width(16.dp).height(4.dp).background(Color.White, CircleShape))
                            Box(modifier = Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_back), "Atrás", tint = Color.White) }
                },
                actions = {
                    TextButton(onClick = onCreateClick) { Text("Omitir", color = Color.White, fontSize = 14.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF8A00))
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(24.dp)) {
                val buttonText = if (invitedUsers.isNotEmpty()) "Crear hogar e invitar (${invitedUsers.size})" else "Crear hogar"
                Button(
                    onClick = onCreateClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painterResource(R.drawable.ic_home), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFF8A00), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)).padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Text("🏠", fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(homeName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text("${invitedUsers.size} miembros invitados", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                            Icon(painterResource(R.drawable.ic_users), contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    // 2. BUSCADOR CONECTADO A LOS ESTADOS
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = { Text("Buscar por nombre o usuario...", color = Color.LightGray, fontSize = 14.sp) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFEEEEEE), focusedBorderColor = Color(0xFFFF8A00), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showPhoneSheet = true },
                            modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                        ) {
                            Icon(painterResource(R.drawable.ic_phone), contentDescription = null, tint = Color(0xFFFF8A00), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Por teléfono", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showEmailSheet = true },
                            modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                        ) {
                            Icon(painterResource(R.drawable.ic_email), contentDescription = null, tint = Color(0xFFFF8A00), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Por correo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (invitedUsers.isNotEmpty()) {
                item { Text("INVITADOS (${invitedUsers.size})", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFFFF8A00), modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)) }
                items(invitedUsers) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6EB)), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFFFF0DC))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(user.avatarColor), contentAlignment = Alignment.Center) {
                                if (user.type == InviteType.PHONE) {
                                    Icon(painterResource(R.drawable.ic_phone), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(user.avatarInitials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (user.type == InviteType.APP) {
                                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF8A00), CircleShape))
                                        Spacer(Modifier.width(4.dp))
                                        Text("App · ", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(user.subtitle, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Box(modifier = Modifier.background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("Pendiente", color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onRemoveInvite(user) }, modifier = Modifier.size(28.dp).background(Color.White, CircleShape).border(1.dp, Color(0xFFEEEEEE), CircleShape)) {
                                Text("✕", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }

            if (availableSuggestions.isNotEmpty()) {
                item { Text("SUGERENCIAS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF9E9E9E), modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) }
                items(availableSuggestions) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(parseHexColor(user.colorHex)), contentAlignment = Alignment.Center) {
                                Text(user.fullName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.username, color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            Box(modifier = Modifier.size(40.dp).background(Color(0xFFFF8A00), CircleShape).clickable { onAddInvite(user.toInvitedUser()) }, contentAlignment = Alignment.Center) {
                                Icon(painterResource(R.drawable.ic_user_cross), contentDescription = "Añadir", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    if (showEmailSheet) {
        ModalBottomSheet(onDismissRequest = { showEmailSheet = false }, containerColor = Color.White, dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Invitar por correo", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showEmailSheet = false }) { Text("✕", color = Color.Gray) }
                }
                Spacer(Modifier.height(8.dp))
                Text("Se enviará un correo de invitación a la dirección indicada.", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = emailInput, onValueChange = { emailInput = it },
                    placeholder = { Text("correo@ejemplo.com", color = Color.LightGray) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_email), contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFEEEEEE), focusedBorderColor = Color(0xFFFF8A00))
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            onAddInvite(
                                InvitedUser(title = emailInput, subtitle = "Correo · $emailInput", type = InviteType.EMAIL, avatarInitials = emailInput.take(1).uppercase(), avatarColor = Color(0xFF9E9E9E))
                            )
                            emailInput = ""
                            showEmailSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                ) {
                    Icon(painterResource(R.drawable.ic_user_cross), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar invitación", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showPhoneSheet) {
        ModalBottomSheet(onDismissRequest = { showPhoneSheet = false }, containerColor = Color.White, dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Invitar por teléfono", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showPhoneSheet = false }) { Text("✕", color = Color.Gray) }
                }
                Spacer(Modifier.height(8.dp))
                Text("Se enviará una invitación por SMS o WhatsApp al número indicado.", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = phoneInput, onValueChange = { phoneInput = it },
                    placeholder = { Text("+52 123 456 7890", color = Color.LightGray) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_phone), contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFEEEEEE), focusedBorderColor = Color(0xFFFF8A00))
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (phoneInput.isNotBlank()) {
                            onAddInvite(
                                InvitedUser(title = phoneInput, subtitle = "Teléfono · $phoneInput", type = InviteType.PHONE, avatarColor = Color(0xFF9E9E9E))
                            )
                            phoneInput = ""
                            showPhoneSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                ) {
                    Icon(painterResource(R.drawable.ic_user_cross), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar invitación", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}