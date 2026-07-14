package com.warp.vpn.ui.screens
 
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.warp.vpn.viewmodel.VPNStatus
import com.warp.vpn.viewmodel.VPNViewModel
 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: VPNViewModel = viewModel()) {
    val status by viewModel.status.collectAsState()
    val configText by viewModel.configText.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
 
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onPermissionResult(result.resultCode == Activity.RESULT_OK)
    }
 
    LaunchedEffect(status) {
        if (status is VPNStatus.NeedPermission) {
            permissionLauncher.launch((status as VPNStatus.NeedPermission).intent)
        }
    }
 
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
 
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WARP VPN") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusCard(status = status)
 
            Spacer(modifier = Modifier.height(24.dp))
 
            OutlinedTextField(
                value = configText,
                onValueChange = viewModel::updateConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                label = { Text("WireGuard Config") },
                placeholder = {
                    Text(
                        "[Interface]\nPrivateKey = ...\nAddress = ...",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                enabled = status !is VPNStatus.Connected,
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
 
            Spacer(modifier = Modifier.height(24.dp))
 
            when (status) {
                is VPNStatus.Connected -> {
                    Button(
                        onClick = { viewModel.disconnect() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Disconnect", style = MaterialTheme.typography.titleMedium)
                    }
                }
                else -> {
                    Button(
                        onClick = { viewModel.connect() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = status !is VPNStatus.Connecting && status !is VPNStatus.NeedPermission
                    ) {
                        if (status is VPNStatus.Connecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Connect WARP", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
 
            Spacer(modifier = Modifier.height(16.dp))
 
            Text(
                text = "Paste your wgcf-profile.conf content above",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
 
@Composable
fun StatusCard(status: VPNStatus) {
    val (icon, title, color) = when (status) {
        is VPNStatus.Disconnected -> Triple("🔒", "Disconnected", Color.Gray)
        is VPNStatus.Connecting -> Triple("⏳", "Connecting...", Color(0xFFFFA000))
        is VPNStatus.Connected -> Triple("🛡️", "Connected", Color(0xFF4CAF50))
        is VPNStatus.NeedPermission -> Triple("⚠️", "Need Permission", Color(0xFFFF5722))
    }
 
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineLarge)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}
 
