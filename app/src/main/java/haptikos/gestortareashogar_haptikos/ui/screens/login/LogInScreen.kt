package haptikos.gestortareashogar_haptikos.ui.screens.login

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.components.CustomTextField
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import haptikos.gestortareashogar_haptikos.utils.authenticateWithBiometric
import haptikos.gestortareashogar_haptikos.utils.findFragmentActivity
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel


@Composable
fun LogInScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: () -> Unit
) {

    LaunchedEffect(authViewModel.isSuccess) {
        if (authViewModel.isSuccess) onNavigateToHome()
    }

    LogInContent(
        errorMessage = authViewModel.errorMessage,
        onLoginClick = { email, password ->
            authViewModel.login(email, password)
        },
        onResetError = {
            authViewModel.resetError()
        },
        onBiometricSuccess = {
            authViewModel.loginWithBiometrics()
        },
        onBiometricError = { errorMessage ->
            authViewModel.showBiometricError(errorMessage)
        }
    )
}

@Composable
fun LogInContent(
    errorMessage: String?,
    onLoginClick:(email: String, password: String) -> Unit,
    onResetError: () -> Unit,
    onBiometricSuccess:() -> Unit,
    onBiometricError:(errorMessage: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context.findFragmentActivity()

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            LogInHeader(modifier = Modifier.padding(top = 40.dp, bottom = 40.dp))

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
                        .padding(24.dp)
                        .navigationBarsPadding(),
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
                        onValueChange = {
                            email = it
                            if (errorMessage != null) onResetError()
                        },
                        label = "Tu correo",
                        placeholder = "correo@ejemplo.com",
                        isError = errorMessage != null,
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_email),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (errorMessage != null) onResetError()
                        },
                        label = "Tu contraseña",
                        placeholder = "********",
                        isError = errorMessage != null,
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_padlock),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                passwordVisible = !passwordVisible
                                }
                            )
                            {
                                Icon(
                                    painter = painterResource(
                                        if (passwordVisible) R.drawable.ic_eye_opened else R.drawable.ic_eye_closed
                                    ),
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Ver contraseña",
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
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage?: "Credenciales inválidas",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            onLoginClick(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
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
                        onClick = {
                            activity?.let { fragmentActivity ->
                                authenticateWithBiometric(
                                    context = fragmentActivity,
                                    onSuccess = {
                                        onBiometricSuccess()
                                    },
                                    onFailed = {
                                        // Si la huella no es correcta
                                        onBiometricError("Huella no reconocida. Intenta de nuevo.")
                                    },
                                    onError = { errorCode, errString ->
                                        // Si hubo cancelación no se muestra ningún error
                                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                                            val personalizedMessage = when (errorCode) {
                                                BiometricPrompt.ERROR_NO_BIOMETRICS ->
                                                    "No tienes ninguna huella registrada en tu teléfono."
                                                BiometricPrompt.ERROR_HW_UNAVAILABLE ->
                                                    "El sensor biométrico no está disponible en este momento."
                                                BiometricPrompt.ERROR_HW_NOT_PRESENT ->
                                                    "Tu dispositivo no tiene sensor de huella."
                                                BiometricPrompt.ERROR_LOCKOUT ->
                                                    "Demasiados intentos fallidos. Sensor bloqueado temporalmente."
                                                BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL ->
                                                    "Debes configurar un PIN o contraseña en tu teléfono primero."
                                                else ->
                                                    "Error de autenticación ($errorCode): $errString"
                                            }

                                            onBiometricError(personalizedMessage)
                                        }
                                    }
                                )
                            }
                        },
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
                            color = MaterialTheme.colorScheme.primary,
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
fun LogInScreenPreview_Normal() {
    GestorTareasHogar_HaptikosTheme {
        LogInContent(
            errorMessage = null,
            onLoginClick = { _, _ -> },
            onResetError = {},
            onBiometricSuccess = {},
            onBiometricError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LogInScreenPreview_Error() {
    GestorTareasHogar_HaptikosTheme {
        LogInContent(
            errorMessage = "Credenciales inválidas",
            onLoginClick = { _, _ -> },
            onResetError = {},
            onBiometricSuccess = {},
            onBiometricError = {}
        )
    }
}