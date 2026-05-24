package com.zfbml.aggregate.network

import java.net.Inet4Address
import java.net.InetAddress
import okhttp3.Dns

object Ipv4FirstDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val addresses = Dns.SYSTEM.lookup(hostname)
        val ipv4 = addresses.filter { it is Inet4Address }
        val others = addresses.filterNot { it is Inet4Address }
        return if (ipv4.isEmpty()) addresses else ipv4 + others
    }
}
