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
    var homeName by remember { mutableStateOf("") }

    CreateHomeStep1Content(
        homeName = homeName,
        onHomeNameChange = { if (it.length <= 40) homeName = it },
        onBack = onBack,
        onNextClick = {
            onNext(homeName, "FF8A00", "", false)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHomeStep1Content(
    homeName: String,
    onHomeNameChange: (String) -> Unit,
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
                        Text(
                            "Crear hogar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(Modifier.width(16.dp).height(4.dp).background(Color.White, CircleShape))
                            Box(Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF8A00),
                    scrolledContainerColor = Color(0xFFFF8A00)
                ),
                actions = { Spacer(Modifier.width(48.dp)) }
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8A00),
                        disabledContainerColor = Color(0xFFFF8A00).copy(alpha = 0.5f)
                    ),
                    enabled = homeName.isNotBlank()
                ) {
                    Text(
                        "Siguiente — Invitar miembros >",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
            // Hero naranja con ícono
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFFF8A00),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏠", fontSize = 40.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Ponle nombre a tu hogar", color = Color.White, fontSize = 14.sp)
                }
            }

            // Campo de nombre
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "NOMBRE DEL HOGAR *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF9E9E9E)
                )
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
                    trailingIcon = {
                        Text(
                            "${homeName.length}/40",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 8.dp)
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

                // Banner informativo
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
                        "Al crear el hogar serás el Creador y podrás gestionar miembros, roles y tareas.",
                        fontSize = 12.sp,
                        color = Color(0xFFD84315),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}