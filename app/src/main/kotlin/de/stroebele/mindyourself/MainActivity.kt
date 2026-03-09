package de.stroebele.mindyourself

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import de.stroebele.mindyourself.ui.navigation.MindYourselfNavGraph
import de.stroebele.mindyourself.ui.theme.MindYourselfTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindYourselfTheme {
                MindYourselfNavGraph()
            }
        }
    }
}
