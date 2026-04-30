package haptikos.gestortareashogar_haptikos.ui.screens.formRoom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity

/**

@Composable
fun FormRoomConfigurationScreen(
    homeName: String,
    members: List<MemberEntity>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            // TopAppBar
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            HomeGeneralSection(homeName)
            HomeMembersSection(members)
            HomeDangerZoneSection()
        }
    }
}
 */