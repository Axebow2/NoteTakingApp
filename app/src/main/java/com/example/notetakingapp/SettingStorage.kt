package com.example.notetakingapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

// Extension property for handling user settings
val Context.settingsDataStore: DataStore<Settings> by dataStore(

    // File where settings are stored
    fileName = "settings.json",

    // Custom serializer for user settings
    serializer = SettingsSerializer
)

// Custom serializer object for handling saving of user data
object SettingsSerializer : Serializer<Settings> {

    override val defaultValue: Settings = Settings()

    override suspend fun readFrom(input: InputStream): Settings {
        return try {
            val jsonString = input.readBytes().decodeToString()
            Json.decodeFromString(Settings.serializer(), jsonString)
        } catch (_: Exception) {
            Settings()
        }
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        val jsonString = Json.encodeToString(Settings.serializer(), t)
        output.write(jsonString.encodeToByteArray())
    }
}