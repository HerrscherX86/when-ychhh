package id.my.f1zz.whenyah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import id.my.f1zz.whenyah.ui.WhenYahApp
import id.my.f1zz.whenyah.ui.theme.WhenYahTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhenYahTheme {
                WhenYahApp()
            }
        }
    }
}
