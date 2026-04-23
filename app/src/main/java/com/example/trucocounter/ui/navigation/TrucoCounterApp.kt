package com.example.trucocounter.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trucocounter.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trucocounter.data.TeamRepository
import com.example.trucocounter.data.remote.RetrofitClient
import com.example.trucocounter.data.remote.TeamDto
import com.example.trucocounter.ui.truco.TrucoViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val HOME = "home"
    const val EQUIPOS = "equipos"
    const val ACERCA_DE = "acerca_de"
}

private data class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private data class Member(
    val name: String,
    val legajo: String,
    val photoResId: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrucoCounterApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val drawerItems = listOf(
        DrawerItem(Routes.HOME, "Inicio", Icons.Default.Home),
        DrawerItem(Routes.EQUIPOS, "Equipos", Icons.Default.Group),
        DrawerItem(Routes.ACERCA_DE, "Acerca de", Icons.Default.Info)
    )

    val currentTitle = drawerItems.firstOrNull { it.route == currentRoute }?.label ?: "Truco Counter"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Truco Counter",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        icon = { Icon(item.icon, contentDescription = null) },
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            AppNavHost(navController = navController, paddingValues = paddingValues)
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Routes.HOME) { HomeScreen() }
        composable(Routes.EQUIPOS) { Lista("Equipos") }
        composable(Routes.ACERCA_DE) { AboutScreen() }
    }
}

@Composable
private fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text("TPO Desarrollo de Aplicaciones 1", style = MaterialTheme.typography.headlineSmall)
        Text("Contador de puntos de Truco", style = MaterialTheme.typography.titleMedium)
        Image(
            painter = painterResource(id = R.drawable.truco),
            contentDescription = "Truco",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun Lista(
    name: String,
    modifier: Modifier = Modifier
) {
    val viewModel: TrucoViewModel = viewModel(
        factory = TrucoViewModel.Factory(TeamRepository(RetrofitClient.apiService))
    )
    val teams by viewModel.teams.observeAsState(emptyList())
    val errorMessage by viewModel.error.observeAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTeam by remember { mutableStateOf<TeamDto?>(null) }
    var deletingTeam by remember { mutableStateOf<TeamDto?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar equipo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(name, style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.syncTeams() }) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Conectar backend")
                }
                Button(onClick = { showResetDialog = true }) {
                    Text("Empezar partida")
                }
            }


            if (teams.isEmpty()) {
                Text("No hay equipos. Agrega uno con +")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(teams, key = { it.id }) { team ->
                    TeamRow(
                        team = team,
                        onAdd = { viewModel.updateTeam(team.copy(puntos = team.puntos + 1)) },
                        onSubtract = {
                            if (team.puntos > 0) {
                                viewModel.updateTeam(team.copy(puntos = team.puntos - 1))
                            }
                        },
                        onEdit = { editingTeam = team },
                        onDelete = { deletingTeam = team }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        TeamDialog(
            title = "Nuevo equipo",
            initialName = "",
            initialPoints = 0,
            onDismiss = { showCreateDialog = false },
            onSave = { nombre, puntos ->
                viewModel.createTeam(nombre, puntos)
                showCreateDialog = false
            }
        )
    }

    editingTeam?.let { team ->
        TeamDialog(
            title = "Editar equipo",
            initialName = team.nombre,
            initialPoints = team.puntos,
            onDismiss = { editingTeam = null },
            onSave = { nombre, puntos ->
                viewModel.updateTeam(team.copy(nombre = nombre, puntos = puntos))
                editingTeam = null
            }
        )
    }

    deletingTeam?.let { team ->
        AlertDialog(
            onDismissRequest = { deletingTeam = null },
            title = { Text("Eliminar equipo") },
            text = { Text("Seguro que queres eliminar a ${team.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTeam(team)
                    deletingTeam = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTeam = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Empezar partida") },
            text = { Text("Reiniciar los puntos de todos los equipos a 0?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAllScores()
                    showResetDialog = false
                }) {
                    Text("Empezar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TeamRow(
    team: TeamDto,
    onAdd: () -> Unit,
    onSubtract: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(team.nombre, fontWeight = FontWeight.SemiBold)
            Text("Puntos: ${team.puntos}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onSubtract, label = { Text("-1") })
                AssistChip(onClick = onAdd, label = { Text("+1") })
                AssistChip(onClick = onEdit, label = { Text("Editar") })
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
private fun TeamDialog(
    title: String,
    initialName: String,
    initialPoints: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var pointsInput by remember(initialPoints) { mutableStateOf(initialPoints.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del equipo") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pointsInput,
                    onValueChange = { pointsInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Puntos") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val puntos = pointsInput.toIntOrNull() ?: 0
                if (name.isNotBlank()) {
                    onSave(name.trim(), puntos)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
private fun AboutScreen() {
    val members = listOf(
        Member(
            name = "Sebastian Andres Deya",
            legajo = "1167157",
            photoResId = R.drawable.sebastian
        ),
        Member(
            name = "Valentina Frisoli",
            legajo = "1167852",
            photoResId = R.drawable.valentina
        ),
        Member(
            name = "Nicolas Llousas",
            legajo = "1147795",
            photoResId = R.drawable.nicolas
        ),
        Member(name = "Integrante pendiente", legajo = "-"),
        Member(name = "Integrante pendiente", legajo = "-")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Acerca de", style = MaterialTheme.typography.headlineSmall)
            Text("Equipo del proyecto")
        }

        items(members) { member ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (member.photoResId != null) {
                            Image(
                                painter = painterResource(id = member.photoResId),
                                contentDescription = "Foto de ${member.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Foto por defecto",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Column {
                        Text(member.name, fontWeight = FontWeight.SemiBold)
                        Text("Legajo: ${member.legajo}")
                    }
                }
            }
        }
    }
}

