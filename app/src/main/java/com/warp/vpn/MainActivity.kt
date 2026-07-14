package com.warp.vpn
 
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.warp.vpn.ui.screens.HomeScreen
import com.warp.vpn.ui.theme.WARPVPNTheme
 
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WARPVPNTheme {
                HomeScreen()
            }
        }
    }
}
