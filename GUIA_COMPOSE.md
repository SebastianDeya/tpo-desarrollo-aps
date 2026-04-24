# Guía Completa: Aplicación Truco Counter con Jetpack Compose

## Índice
1. [Flujo Completo de la App](#flujo-completo)
2. [Explicación de `TrucoCounterApp.kt`](#explicación-de-trucocounterapkkt)
3. [Guía Práctica: Cómo Agregar Más Cosas](#guía-práctica)
4. [Referencia de Componentes y Estilos](#referencia-de-componentes)

---

## Flujo Completo

### 1. **Entrada a la App** (`MainActivity.kt`)
Cuando abres la app, `MainActivity` es el punto de entrada:
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrucoCounterApp()  // Inicia la navegación Compose
        }
    }
}
```

### 2. **Navegación Principal** (`TrucoCounterApp.kt`)
- **`TrucoCounterApp()`**: Configurar la barra superior (TopAppBar) con menú dropdown.
- **`AppNavHost()`**: Gestiona qué pantalla se muestra según la ruta actual.
- **Rutas disponibles**:
  - `"home"` → `HomeScreen()`
  - `"equipos"` → `PantallaEquipos()`
  - `"acerca_de"` → `PantallaAcercaDe()`

### 3. **ViewModel y Datos** (`TrucoViewModel` + `TeamRepository`)
Cuando entras en `PantallaEquipos`:
- `TrucoViewModel` se crea con `viewModel()`.
- Conecta con `TeamRepository` que habla con `RetrofitClient`.
- Los datos vienen de **MockAPI** (API remota).

**Flujo de datos:**
```
UI (PantallaEquipos) 
  ↓ observeAsState()
ViewModel (TrucoViewModel)
  ↓ 
Repository (TeamRepository)
  ↓
API (MockAPI)
```

### 4. **Pantallas**
- **Home**: Muestra título e imagen del proyecto.
- **Equipos**: Lista equipos, agregar/editar/eliminar, conectar backend, reiniciar puntajes.
- **Acerca de**: Muestra foto y datos de integrantes del equipo.

---

## Explicación de `TrucoCounterApp.kt`

### **1. `TrucoCounterApp()` - La función principal**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrucoCounterApp() {
    // Estado del navegador
    val navController = rememberNavController()
    
    // Estado del menú dropdown (abierto/cerrado)
    var expanded by remember { mutableStateOf(false) }
    
    // Obtener ruta actual
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    
    // Definir items del menú
    val drawerItems = listOf(
        DrawerItem(Routes.HOME, "Inicio", Icons.Default.Home),
        DrawerItem(Routes.EQUIPOS, "Equipos", Icons.Default.Group),
        DrawerItem(Routes.ACERCA_DE, "Acerca de", Icons.Default.Info)
    )
    
    // Título dinámico según ruta
    val currentTitle = drawerItems.firstOrNull { it.route == currentRoute }?.label ?: "Truco Counter"
    
    // Scaffold: estructura principal con TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menu")
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            drawerItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.label) },
                                    leadingIcon = { Icon(item.icon, contentDescription = null) },
                                    onClick = {
                                        navController.navigate(item.route)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        AppNavHost(navController = navController, paddingValues = paddingValues)
    }
}
```

**Conceptos clave:**
- **`remember { mutableStateOf() }`**: Almacena estado que persiste entre recomposiciones.
- **`navController.navigate()`**: Navega a una ruta.
- **`Scaffold`**: Estructura base de Material Design (TopAppBar, FAB, Snackbar).
- **`DropdownMenu`**: Menú desplegable con items.

---

### **2. `AppNavHost()` - Sistema de navegación**

```kotlin
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
        composable(Routes.EQUIPOS) { PantallaEquipos() }
        composable(Routes.ACERCA_DE) { PantallaAcercaDe() }
    }
}
```

**Qué hace:**
- Define qué pantalla mostrar según la ruta.
- `paddingValues` reserva espacio para el TopAppBar.

---

### **3. `HomeScreen()` - Pantalla de inicio**

```kotlin
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
```

**Componentes:**
- **`Column`**: Contenedor vertical para texto e imagen.
- **`fillMaxSize()`**: Ocupa todo el espacio disponible.
- **`padding(16.dp)`**: Espacio interno de 16 píxeles.
- **`Arrangement.spacedBy()`**: Espacio de 12 píxeles entre elementos.
- **`Image`**: Carga imagen desde recursos.

---

### **4. `PantallaEquipos()` - Gestión de equipos**

```kotlin
@Composable
private fun PantallaEquipos() {
    // ViewModel con datos remotos
    val viewModel: TrucoViewModel = viewModel(
        factory = TrucoViewModel.Factory(TeamRepository(RetrofitClient.apiService))
    )
    
    // Observar datos del ViewModel
    val teams by viewModel.teams.observeAsState(emptyList())
    val errorMessage by viewModel.error.observeAsState()
    
    // Estados locales para diálogos
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTeam by remember { mutableStateOf<TeamDto?>(null) }
    var deletingTeam by remember { mutableStateOf<TeamDto?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    
    // Estado para notificaciones
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar error cuando llega
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            // Botones de acciones
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.syncTeams() }) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Text("Conectar backend")
                }
                Button(onClick = { showResetDialog = true }) {
                    Text("Empezar partida")
                }
            }
            
            // Lista de equipos
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(teams, key = { it.id }) { team ->
                    TeamRow(...)
                }
            }
        }
    }
    
    // Diálogos
    if (showCreateDialog) { TeamDialog(...) }
    editingTeam?.let { TeamDialog(...) }
    deletingTeam?.let { AlertDialog(...) }
    if (showResetDialog) { AlertDialog(...) }
}
```

**Conceptos:**
- **`observeAsState()`**: Observa LiveData del ViewModel y recomponerse al cambiar.
- **`remember`**: Mantiene estado local entre recomposiciones.
- **`LaunchedEffect`**: Ejecuta código cuando el mensaje de error cambia.
- **`LazyColumn`**: Lista eficiente (solo renderiza items visibles).
- **`FloatingActionButton` (FAB)**: Botón flotante para agregar.

---

### **5. `TeamRow()` - Fila de equipo**

```kotlin
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
```

**Componentes:**
- **`Card`**: Contenedor con sombra y bordes.
- **`AssistChip`**: Botón pequeño/chip.
- **`fontWeight.SemiBold`**: Texto semi-negrita.

---

### **6. `PantallaAcercaDe()` - Información del equipo**

```kotlin
@Composable
private fun PantallaAcercaDe() {
    val members = listOf(
        Member("Sebastian Andres Deya", "1167157", R.drawable.sebastian),
        Member("Valentina Frisoli", "1167852", R.drawable.valentina),
        // ...
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
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp))
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
```

**Conceptos:**
- **`Box`**: Contenedor simple para posicionamiento.
- **`clip()`**: Recorta contenido a forma.
- **`ContentScale.Crop`**: Recorta imagen para llenar espacio.

---

## Guía Práctica

### **Agregar un nuevo DropdownMenuItem**

Edita `TrucoCounterApp()` en la sección de `drawerItems`:

```kotlin
// Agregar a la lista de drawerItems:
val drawerItems = listOf(
    DrawerItem(Routes.HOME, "Inicio", Icons.Default.Home),
    DrawerItem(Routes.EQUIPOS, "Equipos", Icons.Default.Group),
    DrawerItem(Routes.ACERCA_DE, "Acerca de", Icons.Default.Info),
    DrawerItem(Routes.ESTADISTICAS, "Estadísticas", Icons.Default.BarChart)  // ← NUEVO
)
```

Luego agrega la ruta en `Routes`:
```kotlin
private object Routes {
    const val HOME = "home"
    const val EQUIPOS = "equipos"
    const val ACERCA_DE = "acerca_de"
    const val ESTADISTICAS = "estadisticas"  // ← NUEVO
}
```

Y en `AppNavHost`:
```kotlin
NavHost(...) {
    composable(Routes.HOME) { HomeScreen() }
    composable(Routes.EQUIPOS) { PantallaEquipos() }
    composable(Routes.ACERCA_DE) { PantallaAcercaDe() }
    composable(Routes.ESTADISTICAS) { PantallaEstadisticas() }  // ← NUEVO
}
```

---

### **Agregar un TextField (caja de texto)**

```kotlin
var nombre by remember { mutableStateOf("") }

OutlinedTextField(
    value = nombre,
    onValueChange = { nombre = it },
    label = { Text("Ingresa nombre") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,  // Solo una línea
    placeholder = { Text("Ej: Juan") }
)
```

**Parámetros útiles:**
- `value`: El texto actual.
- `onValueChange`: Lambda cuando cambia el texto.
- `label`: Etiqueta flotante.
- `placeholder`: Texto de ayuda.
- `singleLine`: true para una línea, false para múltiples.
- `modifier`: Estilos (ancho, altura, padding).

---

### **Cambiar colores**

**En componentes:**
```kotlin
Button(
    onClick = { /* ... */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = Color.Blue,
        contentColor = Color.White
    )
) {
    Text("Clickeame")
}
```

**Usar colores del tema:**
```kotlin
Box(
    modifier = Modifier
        .size(100.dp)
        .background(MaterialTheme.colorScheme.primary)
)
```

---

### **Cambiar tamaño de texto**

```kotlin
Text(
    "Texto grande",
    fontSize = 24.sp,  // 24 scale-independent pixels
    fontWeight = FontWeight.Bold
)

Text(
    "Texto pequeño",
    style = MaterialTheme.typography.labelSmall
)
```

**Estilos predefinidos:**
- `headlineSmall`, `headlineMedium`, `headlineLarge`
- `titleSmall`, `titleMedium`, `titleLarge`
- `bodySmall`, `bodyMedium`, `bodyLarge`
- `labelSmall`, `labelMedium`, `labelLarge`

---

### **Cambiar padding y márgenes**

```kotlin
Column(
    modifier = Modifier
        .padding(16.dp)  // Padding interno
        .fillMaxWidth()
) {
    Text("Con padding interno de 16dp")
}

Button(
    onClick = { },
    modifier = Modifier
        .padding(8.dp)  // Margin externo
        .fillMaxWidth()
) {
    Text("Botón")
}
```

**Padding específico:**
```kotlin
Modifier.padding(
    start = 16.dp,
    top = 8.dp,
    end = 16.dp,
    bottom = 8.dp
)
```

---

### **Agregar una Column nueva**

```kotlin
Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,  // Centra horizontalmente
    verticalArrangement = Arrangement.spacedBy(12.dp)    // Espacio entre items
) {
    Text("Título")
    TextField(value = "", onValueChange = {})
    Button(onClick = {}) { Text("Guardar") }
}
```

---

### **Agregar una Row nueva**

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),  // Espacio horizontal
    verticalAlignment = Alignment.CenterVertically       // Centra verticalmente
) {
    Button(onClick = {}) { Text("-1") }
    Button(onClick = {}) { Text("+1") }
    Button(onClick = {}) { Text("Editar") }
}
```

---

## Referencia de Componentes

| Componente | Uso | Ejemplo |
|---|---|---|
| `Text` | Mostrar texto | `Text("Hola")` |
| `Button` | Botón interactivo | `Button(onClick = { })` |
| `IconButton` | Botón con icono | `IconButton(onClick = {})` |
| `OutlinedTextField` | Caja de texto | `OutlinedTextField(value = "", onValueChange = {})` |
| `Card` | Contenedor con sombra | `Card { Column() }` |
| `Column` | Contenedor vertical | `Column { ... }` |
| `Row` | Contenedor horizontal | `Row { ... }` |
| `LazyColumn` | Lista eficiente | `LazyColumn { items(...) }` |
| `Image` | Mostrar imagen | `Image(painter = painterResource(...))` |
| `Icon` | Mostrar icono | `Icon(Icons.Default.Home)` |
| `FloatingActionButton` | Botón flotante | `FloatingActionButton(onClick = {})` |
| `AlertDialog` | Cuadro de diálogo | `AlertDialog(onDismissRequest = {})` |
| `DropdownMenu` | Menú desplegable | `DropdownMenu(expanded = true)` |

---

## Resumen Rápido

1. **Para navegar**: `navController.navigate("ruta")`
2. **Para estado local**: `var x by remember { mutableStateOf(valor) }`
3. **Para observar ViewModel**: `val datos by viewModel.datos.observeAsState()`
4. **Para estilos**: `modifier = Modifier.fillMaxWidth().padding(16.dp)`
5. **Para listas**: `LazyColumn { items(lista) { item -> ... } }`

¡Ahora estás listo para extender la app! 🚀

