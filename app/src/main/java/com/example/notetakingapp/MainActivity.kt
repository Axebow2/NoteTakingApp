package com.example.notetakingapp

import android.R
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notetakingapp.ui.theme.NoteTakingAppTheme
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import androidx.compose.material.icons.filled.GridView
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.notetakingapp.ui.theme.noteColors
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.Boolean
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp


// Data class for notes
@Serializable
data class Note(
    val id: Long = System.currentTimeMillis(),
    var title: String = "",
    var content: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    var isFavourite: Boolean = false,
    var colorIndex: Int = 0
)

// Data class for user settings
@Serializable
data class Settings(
    val viewMode: String = "LIST",
    val sortBy: String = "ALPHABET",
    val darkMode: Boolean = false,
    val textStyleVisible: Boolean = true,
    val searchBarVisible: Boolean = true,
    val viewToggleVisible: Boolean = true,
    val sortByVisible: Boolean = true,
    val favouritesVisible: Boolean = true,
    val readOnlyVisible: Boolean = true,
    val dateVisible: Boolean = true,
    val colorVisible: Boolean = true
)

enum class ViewMode { LIST, GRID }
enum class SortBy { DATEDES, DATEASC,  ALPHABET, RANDOM }

class NotesViewModel(private val context: Context) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings

    init {
        viewModelScope.launch {
            context.notesDataStore.data.collect {
                _notes.value = it
            }
        }

        viewModelScope.launch {
            context.settingsDataStore.data.collect { settings ->
                _settings.value = settings
            }
        }
    }

    // Function to add a new note to the list
    fun addNote(note: Note) {
        val updated = _notes.value + note
        saveNotes(updated)
    }

    // Function to update existing note
    fun updateNote(updatedNote: Note) {
        val updated = _notes.value.map { if (it.id == updatedNote.id) updatedNote else it }
        saveNotes(updated)
    }

    // Function to erase note
    fun deleteNote(id: Long) {
        val updated = _notes.value.filter { it.id != id }
        saveNotes(updated)
    }

    // Function to find a note by it's unique id
    fun getNoteById(id: Long): Note? = _notes.value.find { it.id == id }


    // Function to save notes to data store
    private fun saveNotes(notes: List<Note>) {
        viewModelScope.launch {
            context.notesDataStore.updateData { notes }
        }
    }

    // Function to toggle view mode between list and grid
    fun toggleViewMode() {
        viewModelScope.launch {
            context.settingsDataStore.updateData { currentSettings ->
                currentSettings.copy(
                    viewMode = if (currentSettings.viewMode == "LIST") "GRID" else "LIST"
                )
            }
        }
    }

    // Function to select which order the notes are presented in
    fun toggleSortMode() {
        viewModelScope.launch {
            context.settingsDataStore.updateData { currentSettings ->
                val newSortBy = when (SortBy.valueOf(currentSettings.sortBy)) {
                    SortBy.DATEDES -> SortBy.DATEASC
                    SortBy.DATEASC -> SortBy.ALPHABET
                    SortBy.ALPHABET -> SortBy.RANDOM
                    SortBy.RANDOM -> SortBy.DATEDES
                }
                currentSettings.copy(sortBy = newSortBy.name)
            }
        }
    }



    // Function to update user settings
    fun updateSettings(update: (Settings) -> Settings) {
        viewModelScope.launch {
            context.settingsDataStore.updateData { currentSettings ->
                update(currentSettings)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteTakingAppTheme {
                AppNavigation()
            }
        }
    }
}

// Function used to manage navigation between pages
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val notesViewModel: NotesViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(context.applicationContext) as T
        }
    })

    NavHost(navController = navController, startDestination = "home_screen") {
        composable("home_screen") {
            HomePage(navController, notesViewModel)
        }
        composable("settings_screen") {
            SettingsPage(navController, notesViewModel)
        }
        composable("create_note_screen") {
            EditNotePage(navController, null, notesViewModel)
        }
        composable("edit_note_screen/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull()
            EditNotePage(navController, noteId, notesViewModel)
        }
    }
}

// Header for all pages
@Composable
fun Header(navController: NavController, viewModel: NotesViewModel) {
    val settings by viewModel.settings.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(if (settings.darkMode){Color(0xFF2F2F2F)} else {Color(0xFF1e3e73) })
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Home Button
        IconButton(onClick = {navController.navigate("home_screen")})
        {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        // Settings Button
        IconButton(onClick = {navController.navigate("settings_screen")})
        {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

// Home page specific sub header
@Composable
fun HomeSubHeader(
        viewMode: ViewMode,
        onToggleView: () -> Unit,
        sortBy: SortBy,
        onToggleSort: () -> Unit,
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
        viewModel: NotesViewModel
    ) {
    val settings by viewModel.settings.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF435f8c)),
        contentAlignment = Alignment.CenterStart
    )
    {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (settings.darkMode) {
                        Color(0xFF494A4A)
                    } else {
                        Color(0xFF435f8c)
                    }
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom search bar
            Box(modifier = Modifier.weight(1f)) {
                if (settings.searchBarVisible) {
                    CustomSearchBar(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            // Row to store buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (settings.sortByVisible) {
                    val label = when (sortBy) {
                        SortBy.ALPHABET -> "A"
                        SortBy.RANDOM -> "R"
                        SortBy.DATEDES, SortBy.DATEASC -> "D"
                    }

                    val sortIcon = when (sortBy) {
                        SortBy.ALPHABET -> Icons.Default.KeyboardArrowDown
                        SortBy.RANDOM -> Icons.Default.Casino
                        SortBy.DATEDES -> Icons.Default.KeyboardDoubleArrowDown
                        SortBy.DATEASC -> Icons.Default.KeyboardDoubleArrowUp
                    }

                    // Toggle note sort by
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                        IconButton(
                            onClick = onToggleSort,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = sortIcon,
                                contentDescription = "Toggle Note Sort Type",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Toggle list or grid view
                if (settings.viewToggleVisible) {
                    IconButton(onClick = onToggleView) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.LIST) Icons.Filled.Menu else Icons.Filled.GridView,
                            contentDescription = "Toggle View",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
// Button to toggle different text types
@Composable
fun ToggleButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Color.Gray else Color.LightGray
        ),
        modifier = Modifier
            .padding(4.dp)
            .width(30.dp)
            .height(30.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text)
    }
}

// Custom search bar
@Composable
fun CustomSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(4.dp))
            .height(36.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = "Search…",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 14.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                innerTextField()
            }
        )
    }
}

// Composable to allow users to change their settings
@Composable
fun SettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    viewModel: NotesViewModel
) {
    val settings by viewModel.settings.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkmarkColor = Color.White,
                uncheckedColor = if (settings.darkMode){Color.LightGray} else {Color.DarkGray },
                checkedColor = if (settings.darkMode){Color.DarkGray} else {Color.LightGray }
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (settings.darkMode){Color.White} else {Color.Black })
    }
}

// Note card composable
@Composable
fun NoteCard(
    note: Note,
    settings: Settings,
    viewMode: ViewMode,
    onClick: () -> Unit,
    onFavouriteClick: (Note) -> Unit
) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = formatter.format(Date(note.createdDate))

    val cardBackgroundColor = if (note.isFavourite && settings.favouritesVisible) {
        Color(0xFFffce04) // Golden colour for favourite
    } else {
        if (settings.colorVisible) {
            noteColors.getOrElse(note.colorIndex) { noteColors[0] }
        } else {
            Color(0xFFDCDCDC) // Default colour
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        if (viewMode == ViewMode.GRID) {
            // Grid view layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (settings.favouritesVisible) {

                    IconButton(
                        onClick = { onFavouriteClick(note) },
                        modifier = Modifier.size(45.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favourite",
                            tint = if (note.isFavourite) Color(0xFFf3a50c) else if (settings.darkMode) Color.DarkGray else Color.Gray,
                            modifier = Modifier.size(45.dp)
                        )
                    }
                }

                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    maxLines = 2
                )

                if (settings.dateVisible) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            // List view layout
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (settings.favouritesVisible) {
                    IconButton(
                        onClick = { onFavouriteClick(note) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favourite",
                            tint = if (note.isFavourite) Color(0xFFf3a50c) else if (settings.darkMode) Color.DarkGray else Color.Gray,
                            modifier = Modifier
                                .width(45.dp)
                                .height(45.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = note.title, style = MaterialTheme.typography.titleMedium)
                    if (settings.dateVisible) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.titleSmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }
    }
}

// Note page specific sub header
@Composable
fun NoteSubHeader(
    onDeleteClick: (() -> Unit)? = null,
    onColorCycle: (() -> Unit)? = null,
    viewModel: NotesViewModel
) {
    val settings by viewModel.settings.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (settings.darkMode){Color(0xFF494A4A)} else {Color(0xFF435f8c) }),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // Cycle Color Button
            if (settings.colorVisible) {
                if (onColorCycle != null) {
                    IconButton(onClick = onColorCycle) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Cycle Color",
                            tint = Color.White
                        )
                    }
                }
            }


            // Delete button (if available)
            if (onDeleteClick != null) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Note",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


// Home Page
@Composable
fun HomePage(navController: NavController, viewModel: NotesViewModel) {
    val notes by viewModel.notes.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Filter notes by title
    val filteredNotes = notes
        .filter { it.title.contains(searchQuery, ignoreCase = true) }
        .let { list ->
            when (SortBy.valueOf(settings.sortBy)) {
                SortBy.ALPHABET -> list.sortedBy { it.title.lowercase() }
                SortBy.RANDOM -> list.shuffled()
                SortBy.DATEDES -> list.sortedByDescending { it.createdDate }
                SortBy.DATEASC -> list.sortedBy { it.createdDate }
            }
        }

    Scaffold(
        topBar = {
            Column {
                Header(navController, viewModel)
                HomeSubHeader(
                    viewMode = ViewMode.valueOf(settings.viewMode),
                    onToggleView = { viewModel.toggleViewMode() },
                    sortBy = SortBy.valueOf(settings.sortBy),
                    onToggleSort = { viewModel.toggleSortMode() },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    viewModel = viewModel
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(if (settings.darkMode) Color(0xFF737373) else Color(0xFFFFFFFF))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Display notes in list view
                    if (ViewMode.valueOf(settings.viewMode) == ViewMode.LIST) {
                        LazyColumn(modifier = Modifier.padding(1.dp)) {
                            items(filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    settings = settings,
                                    viewMode = ViewMode.LIST,
                                    onClick = { navController.navigate("edit_note_screen/${note.id}") },
                                    onFavouriteClick = { toggledNote ->
                                        val updatedNote = toggledNote.copy(isFavourite = !toggledNote.isFavourite)
                                        viewModel.updateNote(updatedNote)
                                    }
                                )
                            }
                        }
                    } else {
                        // Display notes in grid view
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.padding(8.dp),
                            contentPadding = PaddingValues(4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    settings = settings,
                                    viewMode = ViewMode.GRID,
                                    onClick = { navController.navigate("edit_note_screen/${note.id}") },
                                    onFavouriteClick = { toggledNote ->
                                        val updatedNote = toggledNote.copy(isFavourite = !toggledNote.isFavourite)
                                        viewModel.updateNote(updatedNote)
                                    }
                                )
                            }
                        }
                    }
                }

                // New note button
                Button(
                    onClick = { navController.navigate("create_note_screen") },
                    shape = RoundedCornerShape(20),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.darkMode) Color(0xFF494A4A) else Color(0xFF435f8c),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(9.dp)
                        .height(65.dp)
                        .width(140.dp)
                ) {
                    Text("New Note +", style = TextStyle(fontSize = 16.sp))
                }
            }
        })
}


// Settings Page
@Composable
fun SettingsPage(navController: NavController, viewModel: NotesViewModel) {
    val settings by viewModel.settings.collectAsState()


    Column(modifier = Modifier.fillMaxSize().background(if (settings.darkMode){Color(0xFF737373)} else {Color(0xFFFFFFFF) })) {
        // Header
        Header(navController, viewModel)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Enable and Disable Features",
                style = MaterialTheme.typography.titleMedium,
                color = if(settings.darkMode){Color.White}else{Color.Gray},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp,bottom = 10.dp)
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )

            // Checklist of settings

            // Dark Mode Settings
            SettingItem(
                title = "Enable Dark Mode",
                checked = settings.darkMode,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(darkMode = newValue) }
                },
                viewModel = viewModel
            )

            // Home Page Settings

            SettingItem(
                title = "Show Search Bar",
                checked = settings.searchBarVisible,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(searchBarVisible = newValue) }
                },
                viewModel = viewModel
            )

            SettingItem(
                title = "Show List or Grid Toggle",
                checked = settings.viewToggleVisible,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(viewToggleVisible = newValue) }
                },
                viewModel = viewModel
            )

            SettingItem(
                title = "Show Note Sorting Options",
                checked = settings.sortByVisible,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(sortByVisible = newValue) }
                },
                viewModel = viewModel
            )

            SettingItem(
                title = "Show Favourites Feature",
                checked = settings.favouritesVisible,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(favouritesVisible = newValue) }
                },
                viewModel = viewModel
            )

            SettingItem(
                title = "Show Dates Under Notes",
                checked = settings.dateVisible,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(dateVisible = newValue) }
                },
                viewModel = viewModel
            )


            // Note Taking Page Settings

            SettingItem(
                title = "Show Text Style Options",
                checked = settings.textStyleVisible,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(textStyleVisible = newValue) }
                },
                viewModel = viewModel
            )

            SettingItem(
                title = "Show Custom Note Colors",
                checked = settings.colorVisible,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(colorVisible = newValue) }
                },
                viewModel = viewModel
            )

            SettingItem(
                title = "Show Read Only Mode Option",
                checked = settings.readOnlyVisible,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings { it.copy(readOnlyVisible = newValue) }
                },
                viewModel = viewModel
            )

        }
    }
}


// Note taking page
@Composable
fun EditNotePage(navController: NavController, noteId: Long?, viewModel: NotesViewModel) {
    val existingNote = noteId?.let { viewModel.getNoteById(it) }
    val isNewNote = existingNote == null
    var note by remember { mutableStateOf(existingNote ?: Note()) }
    var hasBeenAdded by remember { mutableStateOf(!isNewNote) }
    val settings by viewModel.settings.collectAsState()

    val currentColorIndex = note.colorIndex
    var editNoteColors = listOf(
        Color(0xFFFFFFFF), // Default white
        Color(0x80FFF9C4), // 50% Opacity Yellow
        Color(0x80FFCCBC), // 50% Opacity Orange
        Color(0x80C8E6C9), // 50% Opacity Green
        Color(0x80BBDEFB), // 50% Opacity Blue
        Color(0x80E1BEE7), // 50% Opacity Purple
        Color(0x80F89494), // 50% Opacity Red
        Color(0x80B3E5FC), // 50% Opacity  Cyan
        Color(0x80FFCDD2), // 50% Opacity  Pink
    )
    if (settings.darkMode){
        editNoteColors = listOf(
            Color(0xFF737373), // Default grey
            Color(0xFFB9B590), // 50% Darker Yellow
            Color(0xFFB49287), // 50% Darker Orange
            Color(0xFF92A893), // 50% Darker Green
            Color(0xFF8CA6BB), // 50% Darker Blue
            Color(0xFFA58BA9), // 50% Darker Purple
            Color(0xFFAD6767), // 50% Darker Red
            Color(0xFF7797A6), // 50% Darker Cyan
            Color(0xFFA8888C), // 50% Darker Pink
        )
    }

    val richTextState = rememberRichTextState()


    var boldEnabled by remember { mutableStateOf(false) }
    var italicEnabled by remember { mutableStateOf(false) }
    var underlineEnabled by remember { mutableStateOf(false) }


    LaunchedEffect(note.content) {
        richTextState.setHtml(note.content)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { richTextState.currentSpanStyle }
            .collect { style ->
                boldEnabled = style.fontWeight == FontWeight.Bold
                italicEnabled = style.fontStyle == FontStyle.Italic
                underlineEnabled = style.textDecoration?.contains(TextDecoration.Underline) == true
            }
    }

    Scaffold(
        topBar = {
            Column {
                Header(navController, viewModel)
                NoteSubHeader(
                    onDeleteClick = {
                        viewModel.deleteNote(note.id)
                        navController.popBackStack()
                    } ,
                    onColorCycle = {
                        val newIndex = (currentColorIndex + 1) % noteColors.size
                        val updatedNote = note.copy(colorIndex = newIndex)
                        viewModel.updateNote(updatedNote)
                        note = updatedNote
                    },
                    viewModel = viewModel
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background( if (settings.colorVisible) {editNoteColors[currentColorIndex]} else {Color(0xFFFFFFFF)})
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp)
        ) {
            // Title Field
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = note.title,
                    onValueChange = {
                        note = note.copy(title = it)
                        if (it.isNotBlank()) {
                            if (!hasBeenAdded) {
                                viewModel.addNote(note)
                                hasBeenAdded = true
                            } else {
                                viewModel.updateNote(note)
                            }
                        }
                    },
                    placeholder = { Text("Title (required for saving)", fontSize = 16.sp) },
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFe1e2ec),
                        unfocusedContainerColor = Color(0xFFe1e2ec),
                        focusedIndicatorColor = Color(0xFFe1e2ec),
                        unfocusedIndicatorColor = Color(0xFFe1e2ec),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Editor
            RichTextEditor(
                state = richTextState,
                placeholder = { Text("Notes go here") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
            )

            val imePaddingModifier = Modifier
                .imePadding()
                .padding(bottom = 200.dp)


            // Style buttons
            if (settings.textStyleVisible) {
            Row(modifier = imePaddingModifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End ) {
                ToggleButton("B", boldEnabled) {
                    richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                }
                ToggleButton("I", italicEnabled) {
                    richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                }
                ToggleButton("U", underlineEnabled) {
                    richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                }
            }
            }


            Spacer(modifier = Modifier.height(160.dp))

        }
    }

    // Auto-save content to Note model
    LaunchedEffect(richTextState.toHtml()) {
        val htmlContent = richTextState.toHtml()
        note = note.copy(content = htmlContent)
        if (note.title.isNotBlank()) {
            if (!hasBeenAdded) {
                viewModel.addNote(note)
                hasBeenAdded = true
            } else {
                viewModel.updateNote(note)
            }
        }
    }

}


