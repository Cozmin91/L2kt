package com.l2kt.gameserver.network

data class SessionKey(val loginOkID1: Int, val loginOkID2: Int, val playOkID1: Int, val playOkID2: Int) {
    override fun toString(): String {
        return "PlayOk: $playOkID1 $playOkID2 LoginOk:$loginOkID1 $loginOkID2"
    }
}