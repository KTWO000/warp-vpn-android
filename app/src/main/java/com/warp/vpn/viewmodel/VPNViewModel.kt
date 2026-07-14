package com.warp.vpn.viewmodel
 
import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import com.warp.vpn.model.WGConfig
import com.warp.vpn.service.WGVPNService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
 
class VPNViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _status = MutableStateFlow<VPNStatus>(VPNStatus.Disconnected)
    val status: StateFlow<VPNStatus> = _status
 
    private val _configText = MutableStateFlow("")
    val configText: StateFlow<String> = _configText
 
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
 
    private val context = application.applicationContext
 
    fun updateConfig(text: String) { _configText.value = text }
 
    fun connect() {
        val config = WGConfig.parse(_configText.value)
        if (config == null) {
            _errorMessage.value = "Invalid WireGuard configuration"
            return
        }
 
        val intent = VpnService.prepare(context)
        if (intent != null) {
            _status.value = VPNStatus.NeedPermission(intent)
            return
        }
 
        _status.value = VPNStatus.Connecting
        _errorMessage.value = null
 
        val serviceIntent = Intent(context, WGVPNService::class.java).apply {
            action = WGVPNService.ACTION_CONNECT
            putExtra(WGVPNService.EXTRA_CONFIG, _configText.value)
        }
        context.startForegroundService(serviceIntent)
        _status.value = VPNStatus.Connected
    }
 
    fun disconnect() {
        val serviceIntent = Intent(context, WGVPNService::class.java).apply {
            action = WGVPNService.ACTION_DISCONNECT
        }
        context.startService(serviceIntent)
        _status.value = VPNStatus.Disconnected
    }
 
    fun onPermissionResult(granted: Boolean) {
        if (granted) connect() else {
            _errorMessage.value = "VPN permission denied"
            _status.value = VPNStatus.Disconnected
        }
    }
 
    fun clearError() { _errorMessage.value = null }
}
 
sealed class VPNStatus {
    object Disconnected : VPNStatus()
    object Connecting : VPNStatus()
    object Connected : VPNStatus()
    data class NeedPermission(val intent: Intent) : VPNStatus()
}
 
