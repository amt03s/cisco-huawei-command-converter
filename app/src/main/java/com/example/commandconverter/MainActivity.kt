package com.example.commandconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.min
import androidx.compose.material3.TextFieldDefaults


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommandConverterApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandConverterApp(viewModel: CommandConverterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var inputCommand by remember { mutableStateOf("") }
    var convertedCommand by remember { mutableStateOf("") }
    var suggestion by remember { mutableStateOf("") }
    var showAddCommandScreen by remember { mutableStateOf(false) }
    var showCommandsScreen by remember { mutableStateOf(false) }

    if (showAddCommandScreen) {
        AddCommandScreen(onBack = { showAddCommandScreen = false }, viewModel)
    } else if (showCommandsScreen) {
        ViewCommandsScreen(onBack = { showCommandsScreen = false }, viewModel)
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White), // White background
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Cisco IOS / Huawei VRP Converter",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black // Black text
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = inputCommand,
                    onValueChange = {
                        inputCommand = it
                        convertedCommand = viewModel.convertCommand(it) ?: "Command not found"
                        suggestion = if (convertedCommand != "Command not found" && convertedCommand != null) "" else viewModel.suggestCommand(it)
                    },
                    label = { Text("Enter command", color = Color.Black) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White, // White background for both focused & unfocused
                        cursorColor = Color.Black,    // Cursor color black
                        focusedIndicatorColor = Color.Black,  // Black underline when focused
                        unfocusedIndicatorColor = Color.Black, // Black underline when not focused
                    ),
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(16.dp))

                Text("Converted: $convertedCommand", style = MaterialTheme.typography.headlineSmall, color = Color.Black)

                val detectedLanguage = when {
                    inputCommand.isEmpty() -> "Unknown"
                    viewModel.commandMap.containsKey(inputCommand) -> "Cisco IOS"
                    viewModel.commandMap.containsValue(inputCommand) -> "Huawei VRP"
                    else -> "Unknown"
                }
                val convertedLanguage = when {
                    inputCommand.isEmpty() -> "Unknown"
                    convertedCommand == "Command not found" -> "Unknown"
                    detectedLanguage == "Cisco IOS" -> "Huawei VRP"
                    detectedLanguage == "Huawei VRP" -> "Cisco IOS"
                    else -> "Unknown"
                }

                Text("Detected Language: $detectedLanguage", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                Text("Converted To: $convertedLanguage", style = MaterialTheme.typography.bodyMedium, color = Color.Black)

                if (suggestion.isNotEmpty()) {
                    Text("Did you mean: $suggestion?", style = MaterialTheme.typography.bodyMedium, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showAddCommandScreen = true },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Add Command")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { showCommandsScreen = true },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("View Commands")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommandScreen(onBack: () -> Unit, viewModel: CommandConverterViewModel) {
    var iosCommand by remember { mutableStateOf("") }
    var vrpCommand by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // White background
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Add Commands",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = iosCommand,
                onValueChange = { iosCommand = it },
                label = { Text("Enter Cisco command", color = Color.Black) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White, // Light gray background
                    cursorColor = Color.Black,    // Cursor color black
                    focusedIndicatorColor = Color.Black,  // Black underline when focused
                    unfocusedIndicatorColor = Color.Black, // Black underline when not focused
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = vrpCommand,
                onValueChange = { vrpCommand = it },
                label = { Text("Enter Huawei command", color = Color.Black) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White, // Light gray background
                    cursorColor = Color.Black,    // Cursor color black
                    focusedIndicatorColor = Color.Black,  // Black underline when focused
                    unfocusedIndicatorColor = Color.Black, // Black underline when not focused
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.addCommand(iosCommand, vrpCommand)
                    onBack()
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Save Command")
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onBack,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
fun ViewCommandsScreen(onBack: () -> Unit, viewModel: CommandConverterViewModel) {
    var commands by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(viewModel) {
        viewModel.loadCommands { loadedCommands ->
            commands = loadedCommands
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White), // White background
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Stored Commands", style = MaterialTheme.typography.headlineSmall, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White) // Make background white
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray), // Light Gray Header Background
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cisco Command", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(8.dp), color = Color.Black)
                        Text("Huawei Command", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(8.dp), color = Color.Black)
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(commands) { (ios, vrp) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White) // White Background for Rows
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(ios, modifier = Modifier.weight(1f), color = Color.Black)
                                Text(vrp, modifier = Modifier.weight(1f), color = Color.Black)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBack,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Back")
            }
        }
    }
}

class CommandConverterViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val commandMap = mutableMapOf<String, String>()
    private val _commands = mutableStateListOf<Pair<String, String>>() // Store commands in ViewModel

    val commands: List<Pair<String, String>> get() = _commands // Expose as immutable list

    init {
        // Ensure commands are loaded when ViewModel initializes
        loadCommands { loadedCommands ->
            _commands.clear()
            _commands.addAll(loadedCommands)
        }
    }

    fun loadCommands(onLoaded: (List<Pair<String, String>>) -> Unit) {
        db.collection("commands").get()
            .addOnSuccessListener { result ->
                commandMap.clear()
                val loadedCommands = mutableListOf<Pair<String, String>>()
                for (document in result) {
                    val iosCommand = document.getString("ios") ?: ""
                    val vrpCommand = document.getString("vrp") ?: ""
                    if (iosCommand.isNotEmpty() && vrpCommand.isNotEmpty()) {
                        commandMap[iosCommand] = vrpCommand
                        loadedCommands.add(iosCommand to vrpCommand)
                    }
                }
                onLoaded(loadedCommands)
            }
            .addOnFailureListener {
                println("Failed to load commands from Firebase")
                onLoaded(emptyList())
            }
    }

    fun addCommand(iosCommand: String, vrpCommand: String) {
        val commandData = hashMapOf("ios" to iosCommand, "vrp" to vrpCommand)

        db.collection("commands").add(commandData)
            .addOnSuccessListener {
                commandMap[iosCommand] = vrpCommand
                commandMap[vrpCommand] = iosCommand
            }
            .addOnFailureListener {
                println("Error adding command")
            }
    }

    fun getCommands(): Map<String, String> {
        return commandMap
    }

    fun convertCommand(command: String): String? {
        return when {
            commandMap.containsKey(command) -> commandMap[command] // Convert from Cisco to Huawei
            commandMap.containsValue(command) -> commandMap.entries.find { it.value == command }?.key // Convert from Huawei to Cisco
            else -> null
        }
    }


    fun suggestCommand(command: String): String {
        val lowerCommand = command.lowercase().trim()

        // If the user hasn't typed anything, don't suggest anything
        if (lowerCommand.isEmpty()) return ""

        var bestMatch: String? = null
        var bestDistance = Int.MAX_VALUE

        // First, check for prefix matches
        for (key in commandMap.keys + commandMap.values) {
            if (key.lowercase().startsWith(lowerCommand)) {
                return key // Immediately return if a perfect prefix match is found
            }
        }

        // If no prefix matches, use Levenshtein Distance as a backup
        for (key in commandMap.keys + commandMap.values) {
            val distance = weightedLevenshtein(lowerCommand, key.lowercase())

            if (distance < bestDistance) {
                bestDistance = distance
                bestMatch = key
            }
        }

        // Suggest only if the match is reasonably close (distance ≤ 2)
        return if (bestDistance <= 2) bestMatch ?: "" else ""
    }





    private fun weightedLevenshtein(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(2) { IntArray(n + 1) }

        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            dp[i % 2][0] = i
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else if (i == 1 || j == 1) 2 else 1 // Heavier penalty for first letter differences
                dp[i % 2][j] = minOf(dp[(i - 1) % 2][j] + 1, dp[i % 2][j - 1] + 1, dp[(i - 1) % 2][j - 1] + cost)
            }
        }
        return dp[m % 2][n]
    }


}