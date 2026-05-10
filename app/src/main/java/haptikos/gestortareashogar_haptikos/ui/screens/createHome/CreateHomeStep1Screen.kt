package haptikos.gestortareashogar_haptikos.ui.screens.createHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun CreateHomeStep1Screen(
    onBack: () -> Unit,
    onNext: (name: String, colorHex: String, desc: String, isPrivate: Boolean) -> Unit
) {
    // Estados elevados a la Screen
    var homeName by remember { mutableStateOf("") }
    var homeDesc by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        Color(0xFFFF6D00), Color(0xFFE91E63), Color(0xFF2962FF),
        Color(0xFF00C853), Color(0xFFAA00FF), Color(0xFF00BFA5),
        Color(0xFFD50000), Color(0xFF3D5AFE)
    )
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }

    CreateHomeStep1Content(
        homeName = homeName,
        onHomeNameChange = { if (it.length <= 40) homeName = it },
        homeDesc = homeDesc,
        onHomeDescChange = { if (it.length <= 120) homeDesc = it },
        colorOptions = colorOptions,
        selectedColor = selectedColor,
        onColorSelected = { selectedColor = it },
        isPrivate = isPrivate,
        onPrivacyChange = { isPrivate = it },
        onBack = onBack,
        onNextClick = {
            val colorHex = String.format("%06X", (0xFFFFFF and selectedColor.toArgb()))
            onNext(homeName, colorHex, homeDesc, isPrivate)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHomeStep1Content(
    homeName: String,
    onHomeNameChange: (String) -> Unit,
    homeDesc: String,
    onHomeDescChange: (String) -> Unit,
    colorOptions: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    isPrivate: Boolean,
    onPrivacyChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onNextClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear hogar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Box(modifier = Modifier.width(16.dp).height(4.dp).background(Color.White, CircleShape))
                            Box(modifier = Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
                            Box(modifier = Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF8A00),
                    scrolledContainerColor = Color(0xFFFF8A00)
                ),
                actions = { Spacer(modifier = Modifier.width(48.dp)) }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8A00),
                        disabledContainerColor = Color(0xFFFF8A00).copy(alpha = 0.5f)
                    ),
                    enabled = homeName.isNotBlank()
                ) {
                    Text("Siguiente — Invitar miembros >", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFFF8A00),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏠", fontSize = 40.sp)
                        }

                        Box(
                            modifier = Modifier
                                .offset(x = 8.dp, y = 8.dp)
                                .size(24.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color(0xFFFF8A00), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("☺", fontSize = 14.sp, color = Color(0xFFFF8A00))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Toca para cambiar el ícono", color = Color.White, fontSize = 14.sp)
                }
            }

            // Formulario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Nombre del Hogar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("NOMBRE DEL HOGAR *", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF9E9E9E))
                    OutlinedTextField(
                        value = homeName,
                        onValueChange = onHomeNameChange,
                        placeholder = { Text("Ej. Mi Casa, Familia Ramos...", color = Color.LightGray) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_lines),
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = { Text("${homeName.length}/40", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp)) },
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
                }

                // Descripción
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DESCRIPCIÓN (opcional)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF9E9E9E))
                    OutlinedTextField(
                        value = homeDesc,
                        onValueChange = onHomeDescChange,
                        placeholder = { Text("Ej. Casa familiar en Cd. Obregón...", color = Color.LightGray) },
                        leadingIcon = {
                            // Ícono ajustado
                            Icon(
                                painter = painterResource(R.drawable.ic_lines),
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = { Text("${homeDesc.length}/120", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp, top = 40.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedBorderColor = Color(0xFFFF8A00),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        minLines = 3,
                        maxLines = 3
                    )
                }

                // Colores
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("COLOR DEL HOGAR", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF9E9E9E))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(colorOptions) { color ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { onColorSelected(color) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color) {
                                    // Ícono ajustado
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = "Seleccionado",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Privacidad
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PRIVACIDAD", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF9E9E9E))

                    // Tarjeta Abierto
                    Card(
                        onClick = { onPrivacyChange(false) },
                        border = if (!isPrivate) BorderStroke(2.dp, Color(0xFFFF8A00)) else null,
                        colors = CardDefaults.cardColors(containerColor = if (!isPrivate) Color.White else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (!isPrivate) 0.dp else 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            // Fondo naranja ajustado
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(if (!isPrivate) Color(0xFFFF8A00) else Color(0xFFF5F5F5), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Candado ajustado
                                Icon(
                                    painter = painterResource(R.drawable.ic_opened_padlock),
                                    contentDescription = null,
                                    tint = if (!isPrivate) Color.White else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Abierto", fontWeight = FontWeight.Bold, color = if (!isPrivate) Color(0xFFD84315) else Color.Black)
                                Text("Cualquiera con el código puede unirse.", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (!isPrivate) {
                                // Círculo de check ajustado
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_circle),
                                    contentDescription = null,
                                    tint = Color(0xFFFF8A00),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Tarjeta Privado
                    Card(
                        onClick = { onPrivacyChange(true) },
                        border = if (isPrivate) BorderStroke(2.dp, Color(0xFFFF8A00)) else null,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isPrivate) 0.dp else 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            // Fondo naranja ajustado
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(if (isPrivate) Color(0xFFFF8A00) else Color(0xFFF5F5F5), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Candado ajustado
                                Icon(
                                    painter = painterResource(R.drawable.ic_padlock),
                                    contentDescription = null,
                                    tint = if (isPrivate) Color.White else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Privado", fontWeight = FontWeight.Bold, color = if (isPrivate) Color(0xFFD84315) else Color.Black)
                                Text("Solo el creador puede invitar miembros.", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (isPrivate) {
                                // Círculo de check ajustado
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_circle),
                                    contentDescription = null,
                                    tint = Color(0xFFFF8A00),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                // Círculo vacío ajustado para que mida lo mismo que el check
                                Box(modifier = Modifier.size(20.dp).border(2.dp, Color(0xFFEEEEEE), CircleShape))
                            }
                        }
                    }
                }

                // Banner de Creador
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sparkles),
                        contentDescription = null,
                        tint = Color(0xFFD84315),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Al crear el hogar serás el Creador y podrás gestionar miembros, roles y tareas.",
                        fontSize = 12.sp,
                        color = Color(0xFFD84315),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}