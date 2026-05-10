package haptikos.gestortareashogar_haptikos.ui.screens.joinHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun InputCodeSection(
    code: String,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    // Estado para saber si el usuario está interactuando con el campo de texto
    var isTextFieldFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "INTRODUCE EL CÓDIGO",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Cuadros responsivos de 8 caracteres
        BasicTextField(
            value = code,
            onValueChange = {
                if (it.length <= 8) onCodeChange(it)
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.onFocusChanged { focusState ->
                isTextFieldFocused = focusState.isFocused
            },
            decorationBox = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until 8) {
                        val char = code.getOrNull(i)?.toString() ?: ""

                        // Lógica cuadro en edición
                        val isCurrentBox = isTextFieldFocused && (i == code.length || (code.length == 8 && i == 7))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .padding(horizontal = 3.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isCurrentBox) 2.dp else 1.dp,
                                    color = if (isCurrentBox) Color(0xFF4A68FF) else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentBox) Color(0xFF4A68FF) else Color.Black
                            )
                        }

                        if (i == 3) {
                            Text(
                                text = "-",
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 6.dp),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            }
        )

        Text(
            text = "Ej: MICA-8F2K",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Alerta de Error
        if (errorMessage != null) {
            Surface(
                color = Color(0xFFFFF5F5),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Ícono de exclamación
                        Icon(
                            painter = painterResource(id = R.drawable.ic_circle_information),
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Código incorrecto",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFE53935),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tip de Pegar
        Surface(
            color = Color(0xFFF0F5FF),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "💡 Puedes pegar el código directamente si lo tienes copiado. El formato es XXXX-XXXX.",
                color = Color(0xFF4A68FF),
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSearch,
            enabled = code.length == 8 && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A68FF),
                disabledContainerColor = Color(0xFFD0D6FF),
                disabledContentColor = Color(0xFF8B9FFF)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscando hogar...")
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar hogar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}