package com.example.notetakingapp

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


data class Note(
    val id: Long = System.currentTimeMillis(),
    var title: String = "",
    var content: String = ""
)

class NotesViewModel : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    fun addNote(note: Note) {
        _notes.value = _notes.value + note
    }

    fun updateNote(updatedNote: Note) {
        _notes.value = _notes.value.map { if (it.id == updatedNote.id) updatedNote else it }
    }

    fun getNoteById(id: Long): Note? {
        return _notes.value.find { it.id == id }
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home_screen") {
        // Home screen route
        composable("home_screen") {
            HomePage(navController)
        }

        // Settings screen route
        composable("settings_screen") {
            SettingsPage(navController)
        }

        // Note edit screen route
        composable("create_note_screen") {
            EditNotePage(navController, null)
        }

        // Specific note route
        composable("edit_note_screen/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull()
            EditNotePage(navController, noteId)
        }
    }

}

@Composable
fun HomePage(navController: NavController, viewModel: NotesViewModel = viewModel()) {
    val notes by viewModel.notes.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        //Header
        Header(navController)

        //Sub-header
        SubHeader()

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

@Composable
fun SubHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF435f8c)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Sub-header for optional features",
            modifier = Modifier.padding(start = 16.dp),
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

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

@Composable
fun EditNotePage(navController: NavController, noteId: Long?, viewModel: NotesViewModel = viewModel()) {
    val note = if (noteId != null) viewModel.getNoteById(noteId) else null
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        //Header
        Header(navController)

        //Sub-header
        SubHeader()


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


