package mx.unam.gacetacu.feature.transport

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.unam.gacetacu.R

@Composable
fun TransportScreen() {
    var tab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.transport_pumabus)) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.transport_pumaagua)) })
        }
        when (tab) {
            0 -> PumaBusTab()
            1 -> PumaAguaTab()
        }
    }
}

@Composable
private fun PumaBusTab() {
    var selectedRoute by remember { mutableStateOf(PumaBusCatalog.ROUTES.first()) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Box {
            OutlinedButton(onClick = { menuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedRoute.name)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                PumaBusCatalog.ROUTES.forEach { route ->
                    DropdownMenuItem(text = { Text(route.name) }, onClick = { selectedRoute = route; menuExpanded = false })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            val res = selectedRoute.drawableRes
            if (res != null) {
                Image(painter = painterResource(res), contentDescription = selectedRoute.name)
            } else {
                Text(stringResource(R.string.transport_no_svg))
            }
        }
    }
}

@Composable
private fun PumaAguaTab() {
    LazyColumn(Modifier.fillMaxSize()) {
        items(PumaAguaCatalog.STATIONS) { station ->
            ListItem(
                headlineContent = { Text(station.name) },
                supportingContent = { Text("${station.lat}, ${station.lng}") },
                leadingContent = { Text("💧") },
            )
            Divider()
        }
    }
}
