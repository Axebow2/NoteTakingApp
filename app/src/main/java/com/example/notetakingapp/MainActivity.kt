package com.example.notetakingapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Data class for notes
@Serializable
data class Note(
    val id: Long = System.currentTimeMillis(),
    var title: String = "",
    var content: String = ""
)


class NotesViewModel(private val context: Context) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init {
        viewModelScope.launch {
            context.notesDataStore.data.collect {
                _notes.value = it
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
            SettingsPage(navController)
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
fun Header(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(Color(0xFF1e3e73))
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
fun HomeSubHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF435f8c)),
        contentAlignment = Alignment.CenterStart
    ) {
        //Temporary content
        Text(
            text = "Sub-header for optional features",
            modifier = Modifier.padding(start = 16.dp),
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

// Note page specific sub header
@Composable
fun NoteSubHeader(onDeleteClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF435f8c)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Delete note button
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
    Column(modifier = Modifier.fillMaxSize()) {
        //Header
        Header(navController)

        //Home Page Sub-header
        HomeSubHeader()

        //Create new note
        Button(
            onClick = { navController.navigate("create_note_screen") },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("New Note")
        }

        //Main content
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(notes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate("edit_note_screen/${note.id}")
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = note.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = note.content.take(50) + "...", maxLines = 1)
                    }
                }
            }
        }
    }
}

// Settings Page
@Composable
fun SettingsPage(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        //Header
        Header(navController)

        //Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(text = "Settings go here")
        }
    }
}

// Note taking page
@Composable
fun EditNotePage(navController: NavController, noteId: Long?, viewModel: NotesViewModel) {
    val note = if (noteId != null) viewModel.getNoteById(noteId) else null
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        //Header
        Header(navController)

        //Note Specific Sub-header
        NoteSubHeader(onDeleteClick = {
            if (note != null) {
                viewModel.deleteNote(note.id)
                navController.popBackStack()
            }
        })


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (note != null) {
                    viewModel.updateNote(note.copy(title = title, content = content))
                } else {
                    viewModel.addNote(Note(title = title, content = content))
                }
                navController.popBackStack()
            }) {
                Text("Save Note")
            }
        }
    }
}


