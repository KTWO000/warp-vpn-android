package com.warp.vpn.service
 
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.warp.vpn.MainActivity
import com.warp.vpn.model.WGConfig
import kotlinx.coroutines.*
 
class WGVPNService : VpnService() {
    
    companion object {
        const val ACTION_CONNECT = "com.warp.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.warp.vpn.DISCONNECT"
        const val EXTRA_CONFIG = "config"
        const val CHANNEL_ID = "warp_vpn_channel"
        const val NOTIFICATION_ID = 1
        private const val TAG = "WGVPNService"
    }
 
    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val configString = intent.getStringExtra(EXTRA_CONFIG)
                configString?.let {
                    WGConfig.parse(it)?.let { config -> startVPN(config) }
                }
            }
            ACTION_DISCONNECT -> stopVPN()
        }
        return START_STICKY
    }
 
    private fun startVPN(config: WGConfig) {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("WARP VPN Connected"))
 
        try {
            val builder = Builder()
                .setSession("WARP VPN")
                .setMtu(config.mtu)
                .addAddress(config.addresses.firstOrNull() ?: "172.16.0.2", 32)
 
            config.allowedIPs.forEach { ip ->
                when {
                    ip.contains("/") -> {
                        val (addr, prefix) = ip.split("/")
                        builder.addRoute(addr, prefix.toInt())
                    }
                    ip == "0.0.0.0/0" -> builder.addRoute("0.0.0.0", 0)
                }
            }
 
            config.dns.forEach { builder.addDnsServer(it) }
 
            vpnInterface = builder.establish()
            Log.i(TAG, "VPN started")
 
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN", e)
            stopSelf()
        }
    }
 
    private fun stopVPN() {
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
 
    override fun onDestroy() {
        super.onDestroy()
        stopVPN()
        serviceScope.cancel()
    }
 
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "WARP VPN", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
 
    private fun createNotification(content: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WARP VPN")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
 
