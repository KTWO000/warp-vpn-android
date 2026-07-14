package com.warp.vpn.utils
 
import com.warp.vpn.model.WGConfig
 
object WGConfigParser {
    fun parse(configString: String): WGConfig? {
        return WGConfig.parse(configString)
    }
}
