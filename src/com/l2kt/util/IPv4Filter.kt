package com.l2kt.util

import com.l2kt.commons.mmocore.IAcceptFilter

import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

class IPv4Filter : IAcceptFilter, Runnable {

    private val _floods = ConcurrentHashMap<Int, FloodHolder>()

    init {
        val t = Thread(this)
        t.name = javaClass.simpleName
        t.isDaemon = true
        t.start()
    }

    override fun accept(sc: SocketChannel): Boolean {
        val hash = hash(sc.socket().inetAddress.address)

        val flood = _floods[hash]
        if (flood != null) {
            val currentTime = System.currentTimeMillis()

            if (flood.tries == -1) {
                flood.lastAccess = currentTime
                return false
            }

            if (flood.lastAccess + 1000 > currentTime) {
                flood.lastAccess = currentTime

                if (flood.tries >= 3) {
                    flood.tries = -1
                    return false
                }

                flood.tries++
            } else
                flood.lastAccess = currentTime
        } else
            _floods[hash] = FloodHolder()

        return true
    }

    override fun run() {
        while (true) {
            val referenceTime = System.currentTimeMillis() - 1000 * 300

            _floods.values.removeIf { f -> f.lastAccess < referenceTime }

            try {
                Thread.sleep(SLEEP_TIME)
            } catch (e: InterruptedException) {
                return
            }

        }
    }

    protected inner class FloodHolder {
        internal var lastAccess = System.currentTimeMillis()
        var tries: Int = 0
    }

    companion object {
        private const val SLEEP_TIME: Long = 5000

        private fun hash(ip: ByteArray): Int {
            return ip[0].toInt() and 0xFF or (ip[1].toInt() shl 8 and 0xFF00) or (ip[2].toInt() shl 16 and 0xFF0000) or (ip[3].toInt() shl 24 and -0x1000000)
        }
    }
}