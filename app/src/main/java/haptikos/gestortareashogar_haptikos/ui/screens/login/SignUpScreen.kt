package haptikos.gestortareashogar_haptikos.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.UserGender
import haptikos.gestortareashogar_haptikos.ui.components.CustomTextField
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import androidx.compose.material3.SelectableDates
import androidx.compose.ui.input.key.type
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var name by remember { mutableStateOf("") }

    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Femenino", "Masculino", "Otro", "Prefiero no decirlo")

    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val minAgeYear = currentYear - 13
    val maxAgeYear = currentYear - 120

    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Input,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val limitMin = Calendar.getInstance()
                limitMin.add(Calendar.YEAR, -13)

                val limitMax = Calendar.getInstance()
                limitMax.add(Calendar.YEAR, -120)

                return utcTimeMillis <= limitMin.timeInMillis && utcTimeMillis >= limitMax.timeInMillis
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in maxAgeYear..minAgeYear
            }
        }
    )

    // Errores
    val isEmailInvalid = email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordInvalid = password.isNotEmpty() && password.length < 8
    val isConfirmPasswordInvalid = confirmPassword.isNotEmpty() && confirmPassword != password

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
            LogInHeader(modifier = Modifier.padding(top = 40.dp, bottom = 40.dp))

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
                    Text(text = "¡Bienvenido! 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    Text(text = "Crea tu cuenta para comenzar a organizar tu hogar", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))

                    // Nombre
                    CustomTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            authViewModel.resetError()
                        },
                        label = "Tu nombre",
                        placeholder = "Juan Pérez",
                        leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_user), contentDescription = null, modifier = Modifier.size(24.dp)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Género
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded }
                    ) {
                        CustomTextField(
                            value = gender,
                            onValueChange = {},
                            label = "Género",
                            placeholder = "Selecciona tu género",
                            readOnly = true,
                            modifier = Modifier.menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_gender),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) }
                        )

                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            genderOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        gender = selectionOption
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Fecha de nacimiento
                    Box {
                        CustomTextField(
                            value = dob,
                            onValueChange = {},
                            label = "Fecha de Nacimiento",
                            placeholder = "DD/MM/AAAA",
                            readOnly = true,
                            leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(24.dp)) }
                        )

                        Spacer(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showDatePicker = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Correo
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.resetError()
                        },
                        label = "Tu correo",
                        placeholder = "correo@ejemplo.com",
                        isError = isEmailInvalid,
                        leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_email), contentDescription = null, modifier = Modifier.size(24.dp)) }
                    )
                    if (isEmailInvalid) {
                        Text(text = "Ingresa un correo válido", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contraseña
                    CustomTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            authViewModel.resetError()
                        },
                        label = "Tu contraseña",
                        placeholder = "Mínimo 8 caracteres",
                        isError = isPasswordInvalid,
                        leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_padlock), contentDescription = null, modifier = Modifier.size(24.dp)) },
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(painter = painterResource(if (passwordVisible) R.drawable.ic_eye_opened else R.drawable.ic_eye_closed), contentDescription = "Ver contraseña", modifier = Modifier.size(24.dp))
                            }
                        }
                    )
                    if (isPasswordInvalid) {
                        Text(text = "Debe tener al menos 8 caracteres", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirmar Contraseña
                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            authViewModel.resetError()
                        },
                        label = "Confirma tu contraseña",
                        placeholder = "Repite tu contraseña",
                        isError = isConfirmPasswordInvalid,
                        leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_padlock), contentDescription = null, modifier = Modifier.size(24.dp)) },
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(painter = painterResource(if (confirmPasswordVisible) R.drawable.ic_eye_opened else R.drawable.ic_eye_closed), contentDescription = "Ver contraseña", modifier = Modifier.size(24.dp))
                            }
                        }
                    )
                    if (isConfirmPasswordInvalid) {
                        Text(text = "Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Al registrarte, aceptas nuestros", color = Color.Gray, fontSize = 12.sp)
                    Text(text = "Términos de Servicio", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable { })
                    Text(text = "y", color = Color.Gray, fontSize = 12.sp)
                    Text(text = "Política de Privacidad", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable { }.padding(bottom = 24.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            if (!isLoading && !isPasswordInvalid && !isConfirmPasswordInvalid && !isEmailInvalid) {
                                val selectedGenderEnum = when (gender) {
                                    "Femenino" -> UserGender.FEMALE
                                    "Masculino" -> UserGender.MALE
                                    else -> UserGender.OTHER
                                }
                                authViewModel.signUp(name, selectedGenderEnum, dob, email, password, confirmPassword)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && !isPasswordInvalid && !isConfirmPasswordInvalid && !isEmailInvalid
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Crear mi cuenta", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(horizontalArrangement = Arrangement.Center) {
                        Text("¿Ya tienes una cuenta? ", color = Color.Gray)
                        Text(
                            text = "Iniciar sesión",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                authViewModel.resetError()
                                onNavigateToLogin()
                            }
                        )
                    }
                }
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDatePicker = false
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                    dob = formatter.format(java.util.Date(millis))
                                }
                            }) { Text("Aceptar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

            }
        }
    }
}