package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    // Lista de todos los hogares del usuario
    val allHomes: StateFlow<List<HomeEntityNew>> = repository.allHomes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Hogar seleccionado
    private val _selectedHome = MutableStateFlow<HomeEntityNew?>(null)
    val selectedHome = _selectedHome.asStateFlow()

    fun selectHome(home: HomeEntityNew) {
        _selectedHome.value = home
    }

    // Creación de hogar
    fun createNewHome(
        name: String,
        userName: String,
        userLastName: String,
        userColor: String
    ) {
        viewModelScope.launch {
            try {
                repository.createHomeAndCreator(
                    homeName = name,
                    creatorName = userName,
                    creatorLastName = userLastName,
                    creatorColorHex = userColor
                )
                // TODO mensaje de confirmación
            } catch (e: Exception) {
                // TODO manejar error
            }
        }
    }
}