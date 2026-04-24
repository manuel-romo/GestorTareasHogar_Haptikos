package haptikos.gestortareashogar_haptikos.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_email),
                contentDescription = "Icono de correo",
                modifier = Modifier.padding(16.dp).size(40.dp),
                tint = Color(0xFFFF8A00)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "HomeTasks",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Tu hogar, más organizado 🏠",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                disabledContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default
        )
    }
}

@Composable
fun LogInScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val orangeMain = Color(0xFFFF8A00)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(orangeMain)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            AppHeader(modifier = Modifier.padding(top = 40.dp, bottom = 40.dp))

            // Card contenedor
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¡Hola de nuevo! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Ingresa para ver tus tareas pendientes",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    )

                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Tu correo",
                        placeholder = "correo@ejemplo.com",
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_email),
                                contentDescription = null,
                                tint = Color(0xFFFF8A00),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Tu contraseña",
                        placeholder = "********",
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_email),
                                contentDescription = null,
                                tint = Color(0xFFFF8A00),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        isPassword = true,
                        trailingIcon = {
                            IconButton(onClick = { /* Acción ver password */ }) {

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_eye),
                                    contentDescription = "Icono de correo",
                                    modifier = Modifier.size(24.dp)
                                )

                            }
                        }
                    )

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable { },
                        textAlign = TextAlign.End,
                        color = orangeMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orangeMain),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Ingresar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // Divisor
                    Row(
                        modifier = Modifier.padding(vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        Text(
                            " O continúa con ",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray)
                    }

                    // Botón de Huella
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {

                        Icon(
                            painter = painterResource(id = R.drawable.ic_dactilar),
                            contentDescription = "Icono de correo",
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear huella digital", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Footer
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text("¿Primera vez aquí? ", color = Color.Gray)
                        Text(
                            text = "Crear cuenta gratis",
                            color = orangeMain,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogInScreenPreview() {
    LogInScreen()
}