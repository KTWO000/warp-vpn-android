package com.warp.vpn.model
 
data class WGConfig(
    val privateKey: String,
    val addresses: List<String>,
    val dns: List<String>,
    val mtu: Int = 1280,
    val peerPublicKey: String,
    val peerEndpoint: String,
    val allowedIPs: List<String>,
    val persistentKeepalive: Int = 25
) {
    companion object {
        fun parse(configString: String): WGConfig? {
            var privateKey = ""
            val addresses = mutableListOf<String>()
            val dns = mutableListOf<String>()
            var mtu = 1280
            var peerPublicKey = ""
            var peerEndpoint = ""
            val allowedIPs = mutableListOf<String>()
            var persistentKeepalive = 25
 
            configString.lines().forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("PrivateKey = ") -> privateKey = trimmed.substringAfter("= ").trim()
                    trimmed.startsWith("Address = ") -> {
                        trimmed.substringAfter("= ").trim().split(",").map { it.trim() }.forEach { addresses.add(it) }
                    }
                    trimmed.startsWith("DNS = ") -> {
                        trimmed.substringAfter("= ").trim().split(",").map { it.trim() }.forEach { dns.add(it) }
                    }
                    trimmed.startsWith("MTU = ") -> mtu = trimmed.substringAfter("= ").trim().toIntOrNull() ?: 1280
                    trimmed.startsWith("PublicKey = ") -> peerPublicKey = trimmed.substringAfter("= ").trim()
                    trimmed.startsWith("Endpoint = ") -> peerEndpoint = trimmed.substringAfter("= ").trim()
                    trimmed.startsWith("AllowedIPs = ") -> {
                        trimmed.substringAfter("= ").trim().split(",").map { it.trim() }.forEach { allowedIPs.add(it) }
                    }
                    trimmed.startsWith("PersistentKeepalive = ") -> {
                        persistentKeepalive = trimmed.substringAfter("= ").trim().toIntOrNull() ?: 25
                    }
                }
            }
 
            return if (privateKey.isNotBlank() && peerPublicKey.isNotBlank()) {
                WGConfig(privateKey, addresses, dns, mtu, peerPublicKey, peerEndpoint, allowedIPs, persistentKeepalive)
            } else null
        }
    }
}
 
