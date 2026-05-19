package haptikos.gestortareashogar_haptikos.ui.screens.createHome

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.navigation.Screen
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.SyncViewModel

@Composable
fun CreateHomeStep3Screen(
    homeName: String,
    homeViewModel: HomeViewModel,
    syncViewModel: SyncViewModel,
    invitedUsers: List<InvitedUser> = emptyList(),
    onFinishClick: () -> Unit
) {
    val isOffline by syncViewModel.isOffline.collectAsState()
    val selectedHome by homeViewModel.selectedHome.collectAsState()

    val inviteCode = remember(selectedHome) {
        selectedHome?.inviteCode
    }

    val context = LocalContext.current

    val handleCopyCode: (String) -> Unit = { code ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Código de Invitación", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
    }

    val handleShareCode: (String) -> Unit = { code ->
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "¡Únete a mi hogar '$homeName' en la app! Usa este código de invitación: $code")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir código de hogar")
        context.startActivity(shareIntent)
    }

    CreateHomeStep3Content(
        homeName = homeName,
        inviteCode = inviteCode,
        isOffline = isOffline,
        invitedUsers = invitedUsers,
        onCopyCodeClick = handleCopyCode,
        onShareCodeClick = handleShareCode,
        onFinishClick = onFinishClick
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHomeStep3Content(
    homeName: String,
    inviteCode: String?,
    isOffline: Boolean,
    invitedUsers: List<InvitedUser>,
    onCopyCodeClick: (String) -> Unit,
    onShareCodeClick: (String) -> Unit,
    onFinishClick: () -> Unit
) {

    val isReady = !inviteCode.isNullOrBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                Button(
                    onClick = onFinishClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                ) {
                    Text("Ir a mi hogar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Encabezado
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_add), // Ajusta a tu R.drawable.ic_check
                        contentDescription = "Éxito",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¡Hogar creado con éxito!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tarjeta de hogar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFF8A00).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏠", fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(homeName, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
                            Text("Eres el creador", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Código de invitación
            item {
                Text(
                    text = "CÓDIGO DE INVITACIÓN",
                    fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (isReady) {
                            // Muestra el código naranja si ya existe
                            Text(
                                text = inviteCode!!,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF8A00),
                                letterSpacing = 4.sp
                            )
                        } else {
                            if (!isOffline) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = Color(0xFFFF8A00)
                                )
                                Text(
                                    "Sincronizando...",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            } else {
                                Text("---", fontSize = 32.sp, color = Color.LightGray)
                                Text("Sin conexión", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onCopyCodeClick(inviteCode!!) },
                                enabled = isReady,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF6EB), contentColor = Color(0xFFFF8A00))
                            ) { Text("Copiar código", fontWeight = FontWeight.Bold) }

                            IconButton(
                                onClick = { onShareCodeClick(inviteCode!!) },
                                enabled = isReady,
                                modifier = Modifier.size(48.dp).background(if (isReady) Color(0xFFFFF6EB) else Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            ) {
                                Icon(painterResource(android.R.drawable.ic_menu_share), contentDescription = null, tint = if (isReady) Color(0xFFFF8A00) else Color.LightGray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Banner contextual
            item {
                val (bannerColor, textColor, bannerText, bannerIcon) = when {
                    isReady -> {
                        Banner(Color(0xFFFFF9E6), Color(0xFFB28000), "El código nunca caduca. Puedes gestionarlo en ajustes.", "💡")
                    }
                    !isOffline -> {
                        Banner(Color(0xFFE3F2FD), Color(0xFF1976D2), "Sincronizando tu nuevo hogar con la nube...", "⏳")
                    }
                    else -> {
                        Banner(Color(0xFFFFF0F0), Color(0xFFD32F2F), "Sin conexión. Conéctate para generar tu código.", "⚠️")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = bannerColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(bannerIcon, fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(text = bannerText, color = textColor, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Invitaciones enviadas
            if (invitedUsers.isNotEmpty()) {
                item {
                    Text(
                        text = "INVITACIONES ENVIADAS (${invitedUsers.size})",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Start
                    )
                }

                items(invitedUsers) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
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
                                Text(user.avatarInitials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(user.subtitle, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Pendiente", color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Banner<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)