package mx.unam.gacetacu.feature.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.unam.gacetacu.GacetaCUApp
import mx.unam.gacetacu.R
import mx.unam.gacetacu.core.ViewModelFactory
import mx.unam.gacetacu.core.model.FacultyCatalog

private val DAY_NAMES = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val app = LocalContext.current.applicationContext as GacetaCUApp
    val viewModel: ScheduleViewModel = viewModel(
        factory = ViewModelFactory { ScheduleViewModel(app.container.scheduleRepository) }
    )
    val schedules by viewModel.schedules.collectAsState()
    val selectedId by viewModel.selectedId.collectAsState()
    val classes by viewModel.classes.collectAsState()

    var facultyMenuExpanded by remember { mutableStateOf(false) }
    var newScheduleFaculty by remember { mutableStateOf(FacultyCatalog.ALL.first { it.id != "rectoria" }) }
    var showAddClassDialog by remember { mutableStateOf(false) }

    LaunchedEffect(schedules) {
        if (selectedId == null && schedules.isNotEmpty()) viewModel.select(schedules.first().id)
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.schedule_title)) })

        // Menú desplegable para elegir/crear el horario de una facultad
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.weight(1f)) {
                OutlinedButton(onClick = { facultyMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(newScheduleFaculty.displayName)
                }
                DropdownMenu(expanded = facultyMenuExpanded, onDismissRequest = { facultyMenuExpanded = false }) {
                    FacultyCatalog.ALL.filter { it.id != "rectoria" }.forEach { faculty ->
                        DropdownMenuItem(
                            text = { Text(faculty.displayName) },
                            onClick = { newScheduleFaculty = faculty; facultyMenuExpanded = false },
                        )
                    }
                }
            }
            Button(onClick = {
                viewModel.createSchedule(
                    newScheduleFaculty.id,
                    newScheduleFaculty.displayName,
                    "Horario ${newScheduleFaculty.displayName}",
                ) {}
            }) { Text(stringResource(R.string.schedule_save)) }
        }

        if (schedules.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                schedules.forEach { schedule ->
                    FilterChip(
                        selected = schedule.id == selectedId,
                        onClick = { viewModel.select(schedule.id) },
                        label = { Text(schedule.facultyName) },
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (selectedId != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.schedule_add_class), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showAddClassDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.schedule_add_class))
                }
            }
        }

        if (classes.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.schedule_empty))
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(classes, key = { it.id }) { c ->
                    ListItem(
                        headlineContent = { Text(c.subject) },
                        supportingContent = {
                            Text("${DAY_NAMES.getOrElse(c.day - 1) { "?" }} · %02d:%02d–%02d:%02d · ${c.room}"
                                .format(c.startHour, c.startMinute, c.endHour, c.endMinute))
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteClass(c) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.schedule_delete))
                            }
                        },
                    )
                    Divider()
                }
            }
        }
    }

    if (showAddClassDialog) {
        AddClassDialog(
            onDismiss = { showAddClassDialog = false },
            onConfirm = { subject, day, sh, sm, eh, em, room ->
                viewModel.addClass(subject, day, sh, sm, eh, em, room)
                showAddClassDialog = false
            },
        )
    }
}

@Composable
private fun AddClassDialog(
    onDismiss: () -> Unit,
    onConfirm: (subject: String, day: Int, sh: Int, sm: Int, eh: Int, em: Int, room: String) -> Unit,
) {
    var subject by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(1) }
    var startHour by remember { mutableStateOf(7) }
    var endHour by remember { mutableStateOf(8) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_add_class)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(subject, { subject = it }, label = { Text(stringResource(R.string.schedule_class_name)) })
                OutlinedTextField(room, { room = it }, label = { Text(stringResource(R.string.schedule_room)) })

                Text(stringResource(R.string.schedule_day))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DAY_NAMES.forEachIndexed { index, label ->
                        FilterChip(selected = day == index + 1, onClick = { day = index + 1 }, label = { Text(label) })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour.toString(),
                        onValueChange = { startHour = it.toIntOrNull()?.coerceIn(0, 23) ?: startHour },
                        label = { Text(stringResource(R.string.schedule_start)) },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = endHour.toString(),
                        onValueChange = { endHour = it.toIntOrNull()?.coerceIn(0, 23) ?: endHour },
                        label = { Text(stringResource(R.string.schedule_end)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (subject.isNotBlank()) onConfirm(subject, day, startHour, 0, endHour, 0, room)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
