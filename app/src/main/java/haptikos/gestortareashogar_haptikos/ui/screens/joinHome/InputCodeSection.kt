package haptikos.gestortareashogar_haptikos.ui.screens.joinHome

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.theme.DarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.DarkText
import haptikos.gestortareashogar_haptikos.ui.theme.IndigoBlue
import haptikos.gestortareashogar_haptikos.ui.theme.LightRed
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.PaleBlue
import haptikos.gestortareashogar_haptikos.ui.theme.Red
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray
import haptikos.gestortareashogar_haptikos.ui.theme.White

@Composable
fun InputCodeSection(
    code: String,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "blink"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "INTRODUCE EL CÓDIGO",
            color = MediumDarkGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusRequester.requestFocus() },
            contentAlignment = Alignment.Center
        ) {
            val textFieldValue = remember(code) {
                TextFieldValue(text = code, selection = TextRange(code.length))
            }

            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val filtered = newValue.text.filter { it.isLetterOrDigit() }.uppercase()
                    if (filtered.length <= 8) onCodeChange(filtered)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
                decorationBox = { it() }
            )

            // Cuadros visuales
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until 8) {
                    val char = code.getOrNull(i)?.toString() ?: ""
                    val isCurrentBox = isFocused && (i == code.length || (code.length == 8 && i == 7))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.85f)
                            .padding(horizontal = 3.dp)
                            .background(White, RoundedCornerShape(8.dp))
                            .border(
                                width = if (isCurrentBox) 2.dp else 1.dp,
                                color = if (isCurrentBox) IndigoBlue else SilverGray,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (char.isNotEmpty()) {
                            Text(char, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                color = if (isCurrentBox) IndigoBlue else DarkText)
                        } else if (isCurrentBox) {
                            // Cursor
                            Box(
                                modifier = Modifier
                                    .width(2.dp).height(22.dp)
                                    .background(IndigoBlue.copy(alpha = cursorAlpha))
                            )
                        }
                    }

                    if (i == 3) {
                        Text("-", color = MediumDarkGray,
                            modifier = Modifier.padding(horizontal = 6.dp),
                            fontSize = 24.sp, fontWeight = FontWeight.Light)
                    }
                }
            }
        }

        Text(
            text = "Ej: MICA-8F2K",
            color = DarkGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Alerta de Error
        if (errorMessage != null) {
            Surface(
                color = LightRed,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_circle_information),
                            contentDescription = "Error",
                            tint = Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Código incorrecto",
                            color = Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        val context = LocalContext.current

        Surface(
            color = PaleBlue,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 Si tienes el código copiado, pégalo aquí.",
                    color = IndigoBlue, fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = IndigoBlue,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val pasted = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: return@clickable
                        val cleaned = pasted.filter { it.isLetterOrDigit() }.uppercase().take(8)
                        if (cleaned.isNotEmpty()) onCodeChange(cleaned)
                    }
                ) {
                    Text("Pegar", color = White, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSearch,
            enabled = code.length == 8 && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoBlue,
                disabledContainerColor = PaleBlue,
                disabledContentColor = Color(0xFF8B9FFF)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
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