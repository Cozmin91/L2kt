package com.l2kt.loginserver

import com.l2kt.Config
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

abstract class FloodProtectedListener @Throws(IOException::class)
constructor(listenIp: String, port: Int) : Thread() {
    private val _log = Logger.getLogger(FloodProtectedListener::class.java.name)

    private val _floodProtection = ConcurrentHashMap<String, ForeignConnection>()
    private val _serverSocket: ServerSocket = if (listenIp == "*") ServerSocket(port) else ServerSocket(port, 50, InetAddress.getByName(listenIp))

    override fun run() {
        var connection: Socket? = null

        while (true) {
            try {
                connection = _serverSocket.accept()
                if (Config.FLOOD_PROTECTION) {
                    var fConnection: ForeignConnection? = _floodProtection[connection!!.inetAddress.hostAddress]
                    if (fConnection != null) {
                        fConnection.connectionNumber += 1
                        if (fConnection.connectionNumber > Config.FAST_CONNECTION_LIMIT && System.currentTimeMillis() - fConnection.lastConnection < Config.NORMAL_CONNECTION_TIME || System.currentTimeMillis() - fConnection.lastConnection < Config.FAST_CONNECTION_TIME || fConnection.connectionNumber > Config.MAX_CONNECTION_PER_IP) {
                            fConnection.lastConnection = System.currentTimeMillis()
                            fConnection.connectionNumber -= 1

                            connection.close()

                            if (!fConnection.isFlooding)
                                _log.warning("Potential Flood from " + connection.inetAddress.hostAddress)

                            fConnection.isFlooding = true
                            continue
                        }

                        if (fConnection.isFlooding)
                        // if connection was flooding server but now passed the check
                        {
                            fConnection.isFlooding = false
                            _log.info(connection.inetAddress.hostAddress + " is not considered as flooding anymore.")
                        }
                        fConnection.lastConnection = System.currentTimeMillis()
                    } else {
                        fConnection = ForeignConnection(System.currentTimeMillis())
                        _floodProtection[connection.inetAddress.hostAddress] = fConnection
                    }
                }
                addClient(connection)
            } catch (e: Exception) {
                try {
                    connection?.close()
                } catch (e2: Exception) {
                }

                if (isInterrupted) {
                    // shutdown?
                    try {
                        _serverSocket.close()
                    } catch (io: IOException) {
                        _log.log(Level.INFO, "", io)
                    }

                    break
                }
            }

        }
    }

    protected class ForeignConnection(var lastConnection: Long) {
        var connectionNumber: Int = 0
        var isFlooding = false

        init {
            connectionNumber = 1
        }
    }

    abstract fun addClient(s: Socket)

    fun removeFloodProtection(ip: String) {
        if (!Config.FLOOD_PROTECTION)
            return

        val fConnection = _floodProtection[ip]
        if (fConnection != null) {
            fConnection.connectionNumber -= 1
            if (fConnection.connectionNumber == 0)
                _floodProtection.remove(ip)
        } else
            _log.warning("Removing a flood protection for a GameServer that was not in the connection map??? :$ip")
    }

    fun close() {
        try {
            _serverSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}