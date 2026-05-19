package haptikos.gestortareashogar_haptikos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew

@Composable
fun MemberAvatar(member: MemberEntityNew, size: Dp = 40.dp) {
    val avatarColor = remember(member.colorHex) {
        try { Color(android.graphics.Color.parseColor(member.colorHex)) }
        catch (e: Exception) { Color.Gray }
    }
    val initials = remember(member.name, member.lastName) {
        "${member.name.firstOrNull() ?: ""}${member.lastName.firstOrNull() ?: ""}".uppercase()
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarColor),
        contentAlignment = Alignment.Center
    ) {
        if (!member.profilePicUrl.isNullOrEmpty()) {
            AsyncImage(
                model = member.profilePicUrl,
                contentDescription = member.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.35f).sp
            )
        }
    }
}