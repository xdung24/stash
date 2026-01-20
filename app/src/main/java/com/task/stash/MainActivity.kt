package com.task.stash

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.task.stash.ui.theme.StashTheme
import android.util.Log

class MainActivity : ComponentActivity() {
    private val TAG = "MyAccessibilityService"
    private lateinit var prefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: MainActivity created.")
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val consented = prefs.getBoolean("accessibility_consent", false)
        
        enableEdgeToEdge()
        setContent {
            StashTheme {
                var showDialog by remember { mutableStateOf(!consented) }
                
                if (showDialog) {
                    AccessibilityConsentDialog(
                        onAccept = {
                            prefs.edit().putBoolean("accessibility_consent", true).apply()
                        },
                        onDeny = {
                            finish() // Close the app
                        }
                    )
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilityConsentDialog(onAccept: () -> Unit, onDeny: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismiss */ },
        title = { Text("Accessibility Permission Required") },
        text = {
            Text(
                "This app uses Android's Accessibility Service to automatically clear recent tasks when the recents screen is opened. " +
                "This allows the app to interact with system UI elements for automation purposes. " +
                "By accepting, you consent to this data access and usage. " +
                "You can revoke this permission at any time in your device's Accessibility settings."
            )
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("Accept")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text("Deny")
            }
        }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Welcome to Task Stash!\n\nTo use the app, enable the Recent Cleaner service in your device's Accessibility settings:\n\nSettings > Accessibility > Task Stash > Recent Cleaner Service",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StashTheme {
        Greeting("Android")
    }
}