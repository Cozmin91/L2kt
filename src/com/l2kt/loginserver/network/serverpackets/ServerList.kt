package com.l2kt.loginserver.network.serverpackets

import com.l2kt.loginserver.GameServerManager
import com.l2kt.loginserver.model.ServerData
import com.l2kt.loginserver.network.LoginClient
import com.l2kt.loginserver.network.gameserverpackets.ServerStatus
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*

class ServerList(client: LoginClient) : L2LoginServerPacket() {
    private val _servers = ArrayList<ServerData>()
    private val _lastServer: Int = client.lastServer

    init {
        for (gsi in GameServerManager.registeredGameServers.values) {
            val status =
                if (gsi.status != ServerStatus.STATUS_GM_ONLY) gsi.status else if (client.accessLevel > 0) gsi.status else ServerStatus.STATUS_DOWN
            val hostName = gsi.hostName

            _servers.add(ServerData(status, hostName!!, gsi))
        }
    }

    public override fun write() {
        writeC(0x04)
        writeC(_servers.size)
        writeC(_lastServer)

        for (server in _servers) {
            writeC(server.serverId)

            try {
                val raw = InetAddress.getByName(server.hostName).address
                writeC(raw[0].toInt() and 0xff)
                writeC(raw[1].toInt() and 0xff)
                writeC(raw[2].toInt() and 0xff)
                writeC(raw[3].toInt() and 0xff)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                writeC(127)
                writeC(0)
                writeC(0)
                writeC(1)
            }

            writeD(server.port)
            writeC(server.ageLimit)
            writeC(if (server.isPvp) 0x01 else 0x00)
            writeH(server.currentPlayers)
            writeH(server.maxPlayers)
            writeC(if (server.status == ServerStatus.STATUS_DOWN) 0x00 else 0x01)

            var bits = 0
            if (server.isTestServer)
                bits = bits or 0x04

            if (server.isShowingClock)
                bits = bits or 0x02

            writeD(bits)
            writeC(if (server.isShowingBrackets) 0x01 else 0x00)
        }
    }
}