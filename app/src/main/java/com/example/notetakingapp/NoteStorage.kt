package com.example.notetakingapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

// Extension property for handling notes
val Context.notesDataStore: DataStore<List<Note>> by dataStore(

    // File where notes are stored
    fileName = "notes.json",

    // Custom serializer for notes list
    serializer = NotesSerializer
)

// Custom serializer object for handling reading and writing of note data
object NotesSerializer : Serializer<List<Note>> {

    override val defaultValue: List<Note> = emptyList()

    override suspend fun readFrom(input: InputStream): List<Note> {
        return try {
            val jsonString = input.readBytes().decodeToString()
            Json.decodeFromString(ListSerializer(Note.serializer()), jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun writeTo(t: List<Note>, output: OutputStream) {
        val jsonString = Json.encodeToString(ListSerializer(Note.serializer()), t)
        output.write(jsonString.encodeToByteArray())
    }
}