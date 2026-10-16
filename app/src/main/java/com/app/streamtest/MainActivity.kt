package com.app.streamtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.app.streamtest.ui.theme.BackgroundColor
import com.app.streamtest.ui.theme.StreamTestTheme
import com.app.streamtest.ui.theme.TextColor
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint lets Hilt inject into this Activity and into the
// ViewModels that its screens ask for with hiltViewModel().
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StreamTestTheme {
                // A plain (non-clickable) TV Surface paints the background and sets
                // the default text color for everything inside it.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    colors = SurfaceDefaults.colors(
                        containerColor = BackgroundColor,
                        contentColor = TextColor,
                    ),
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
