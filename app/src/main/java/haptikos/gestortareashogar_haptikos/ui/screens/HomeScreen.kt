package haptikos.gestortareashogar_haptikos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.TaskEntity

@Composable
fun HomeScreen() {
    val orangeMain = Color(0xFFFF8A00)

    Scaffold(
        bottomBar = { CustomBottomNavigation() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = orangeMain,
                shape = CircleShape,
                modifier = Modifier.size(64.dp).offset(y = 50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "Agregar",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8F8))
        ) {

            item { DashboardHeader() }

            // Selector de días
            item { DaySelector() }

            // Lista de tareas pendientes
            item { SectionTitle("PENDIENTES (2)") }
            items(2) {
                TaskCard(
                    TaskEntity(
                        title = "Limpiar la cocina",
                        date = "Lunes",
                        room = "Cocina",
                        points = 15,
                        members = "Miembro",
                        state = "Pendiente"
                    )
                )
            }

            // Lista de tareas pausadas
            item { SectionTitle("PAUSADAS (1)", Color(0xFFD4A017)) }
            item {
                TaskCard(
                    TaskEntity(
                        title = "Lavar la ropa",
                        date = "Lunes",
                        room = "Lavandería",
                        points = 10,
                        members = "Miembro",
                        state = "Pendiente"
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun DashboardHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFFF8A00),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(24.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_home),
                        contentDescription = "Casa",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Text(" Mi Casa ", color = Color.White, fontWeight = FontWeight.Bold)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dropdown),
                        contentDescription = "Expandir",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(12.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_configuration),
                    contentDescription = "Ajustes",
                    modifier = Modifier.padding(8.dp).size(20.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("¡Hola, María! 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Tienes 6 tareas pendientes esta semana", color = Color.White.copy(0.9f))

        Spacer(Modifier.height(20.dp))

        TextField(
            value = "", onValueChange = {},
            placeholder = { Text("Buscar tareas...", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "Buscar",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "Filtros",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
                )
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(20.dp))

        // Progreso
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progreso del día", color = Color.White, fontWeight = FontWeight.Bold)
            Text("33%", color = Color.White, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { 0.33f },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp).clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(0.3f)
        )
    }
}

@Composable
fun DaySelector() {
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    LazyRow(Modifier.padding(vertical = 16.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(dias) { dia ->
            val isSelected = dia == "Lunes"
            Surface(
                modifier = Modifier.padding(end = 8.dp),
                color = if (isSelected) Color(0xFFFF8A00) else Color(0xFFF0F0F0),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Text(
                    text = dia,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskEntity, isPaused: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Box(
                Modifier.size(28.dp).border(2.dp, Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isPaused) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = "Pausada",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFF8A00)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Fecha",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Text(" ${task.date} ", fontSize = 12.sp, color = Color.Gray)
                    Surface(
                        shape = RoundedCornerShape(4.dp)) {
                        Text(" ${task.room} ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    task.members.forEach { color ->
                        Box(
                            Modifier
                                .size(20.dp)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(8.dp)) {
                    Text(" ⭐ +${task.points} ", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Detalles",
                    modifier = Modifier.size(24.dp),
                    tint = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, color: Color = Color.Gray) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        color = color,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    )
}

@Composable
fun CustomBottomNavigation() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Inicio",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Inicio") },
            selected = true,
            onClick = {}
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stats_1),
                    contentDescription = "Hogar",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Hogar") },
            selected = false,
            onClick = {}
        )

        Spacer(Modifier.weight(1f))

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stats_2),
                    contentDescription = "Stats",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Stats") },
            selected = false,
            onClick = {}
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Perfil",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Perfil") },
            selected = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}