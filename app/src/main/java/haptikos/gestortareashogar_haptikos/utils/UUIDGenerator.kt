package haptikos.gestortareashogar_haptikos.utils

import java.util.UUID

fun generateUniqueId(): String {
    return UUID.randomUUID().toString()
}