package haptikos.gestortareashogar_haptikos.ui.screens.login

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {

    val isSuccess by authViewModel.isSuccess.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onNavigateToHome()
        }
    }

    LogInContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        onLoginClick = { email, password ->
            authViewModel.login(email, password)
        },
        onResetError = {
            authViewModel.resetError()
        },
        onBiometricSuccess = {
            authViewModel.loginWithBiometrics()
        },
        onBiometricError = { errorMsg ->
            authViewModel.showBiometricError(errorMsg)
        },
        onNavigateToSignUp = onNavigateToSignUp
    )
}

@Composable
fun LogInContent(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick:(email: String, password: String) -> Unit,
    onResetError: () -> Unit,
    onBiometricSuccess:() -> Unit,
    onBiometricError:(errorMessage: String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val hasAuthError = errorMessage == "Correo o contraseña incorrectos" ||
            errorMessage == "Llena todos los campos" ||
            errorMessage == "Por favor, ingresa un correo válido"

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
                color = MaterialTheme.colorScheme.surface
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
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Ingresa para ver tus tareas pendientes",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        isError = hasAuthError,
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_email),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                        isError = hasAuthError,
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_padlock),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                            text = errorMessage ?: "Credenciales inválidas",
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
                            if (!isLoading) {
                                onLoginClick(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Ingresar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Divisor
                    Row(
                        modifier = Modifier.padding(vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = " O continúa con ",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // Botón de Huella
                    OutlinedButton(
                        onClick = {
                            activity?.let { fragmentActivity ->
                                authenticateWithBiometric(
                                    context = fragmentActivity,
                                    title = "Iniciar sesión",
                                    subtitle = "Usa tu huella para acceder a tus tareas",
                                    onSuccess = { onBiometricSuccess() },
                                    onFailed = { onBiometricError("Huella no reconocida. Intenta de nuevo.") },
                                    onError = { errorMsg ->
                                        errorMsg?.let { onBiometricError(it) }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {

                        Icon(
                            painter = painterResource(id = R.drawable.ic_fingerprint),
                            contentDescription = "Icono de huella",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Escanear huella digital",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Footer
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = "¿Primera vez aquí? ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Crear cuenta gratis",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                onResetError()
                                onNavigateToSignUp()
                            }
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
            isLoading = false,
            errorMessage = null,
            onLoginClick = { _, _ -> },
            onResetError = {},
            onBiometricSuccess = {},
            onBiometricError = {},
            onNavigateToSignUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LogInScreenPreview_Error() {
    GestorTareasHogar_HaptikosTheme {
        LogInContent(
            isLoading = false,
            errorMessage = "Credenciales inválidas",
            onLoginClick = { _, _ -> },
            onResetError = {},
            onBiometricSuccess = {},
            onBiometricError = {},
            onNavigateToSignUp = {}
        )
    }
}
