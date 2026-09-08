package ai.nexa.app

import ai.nexa.core.design.NexaTheme
import ai.nexa.feature.chat.ChatRoute
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexaTheme { ChatRoute() }
        }
    }
}
