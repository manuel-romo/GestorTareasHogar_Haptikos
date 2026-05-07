package haptikos.gestortareashogar_haptikos.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    // Para determinar si hay un usuario logueado.
    fun getCurrentUser() = auth.currentUser

    // Iniciar sesión
    suspend fun login(email: String, password: String): Boolean {
        return try {
            // La llamada a Firebase se trata como una corrutina
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            // TODO manejar errores específicos
            false
        }
    }

    // Crear cuenta.
    suspend fun register(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Cerrar sesión.
    fun logout() {
        auth.signOut()
    }
}