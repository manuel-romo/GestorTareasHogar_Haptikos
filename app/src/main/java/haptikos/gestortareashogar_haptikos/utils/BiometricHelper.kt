package haptikos.gestortareashogar_haptikos.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun authenticateWithBiometric(
    context: FragmentActivity,
    title: String = "Autenticación requerida",
    subtitle: String = "Usa tu huella para continuar",
    onSuccess: () -> Unit,
    onFailed: () -> Unit,
    onError: (errorMessage: String?) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(context)

    val biometricPrompt = BiometricPrompt(
        context,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(null)
                } else {
                    val personalizedMessage = when (errorCode) {
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> "No tienes ninguna huella registrada en tu teléfono."
                        BiometricPrompt.ERROR_HW_UNAVAILABLE -> "El sensor biométrico no está disponible en este momento."
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> "Tu dispositivo no tiene sensor de huella."
                        BiometricPrompt.ERROR_LOCKOUT -> "Demasiados intentos fallidos. Sensor bloqueado temporalmente."
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> "Debes configurar un PIN o contraseña en tu teléfono primero."
                        else -> "Error de autenticación ($errorCode): $errString"
                    }
                    onError(personalizedMessage)
                }
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText("Cancelar")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}