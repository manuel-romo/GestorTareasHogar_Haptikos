package haptikos.gestortareashogar_haptikos.ui.screens.formHome

enum class HomePermission(val title: String, val description: String, val emoji: String) {
    CREATOR_ONLY("Solo el creador", "Solo tú puedes crear y editar tareas", "👑"),
    ADMINS("Miembros con rol Administrador", "Los miembros con rol Administrador pueden crear y editar", "✏️"),
    ALL_MEMBERS("Todos los miembros", "Cualquier miembro puede crear y editar tareas", "👥")
}