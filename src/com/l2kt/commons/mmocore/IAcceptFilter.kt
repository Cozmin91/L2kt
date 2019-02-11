package com.l2kt.commons.mmocore

import java.nio.channels.SocketChannel

interface IAcceptFilter {
    fun accept(sc: SocketChannel): Boolean
}