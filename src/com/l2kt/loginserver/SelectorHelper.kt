package com.l2kt.loginserver

import com.l2kt.commons.mmocore.*
import com.l2kt.loginserver.network.LoginClient
import com.l2kt.loginserver.network.serverpackets.Init
import com.l2kt.util.IPv4Filter
import java.nio.channels.SocketChannel
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class SelectorHelper : IMMOExecutor<LoginClient>, IClientFactory<LoginClient>, IAcceptFilter {
    private val _generalPacketsThreadPool: ThreadPoolExecutor =
        ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, LinkedBlockingQueue())

    private val _ipv4filter: IPv4Filter = IPv4Filter()

    override fun execute(packet: ReceivablePacket<LoginClient>) {
        _generalPacketsThreadPool.execute(packet)
    }

    override fun create(con: MMOConnection<LoginClient>): LoginClient {
        val client = LoginClient(con)
        client.sendPacket(Init(client))
        return client
    }

    override fun accept(sc: SocketChannel): Boolean {
        return _ipv4filter.accept(sc) && !LoginController.isBannedAddress(sc.socket().inetAddress)
    }
}