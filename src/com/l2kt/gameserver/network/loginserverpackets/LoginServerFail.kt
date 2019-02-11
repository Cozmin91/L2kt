package com.l2kt.gameserver.network.loginserverpackets

class LoginServerFail(decrypt: ByteArray) : LoginServerBasePacket(decrypt) {
    val reason: Int = readC()

    val reasonString: String
        get() = REASONS[reason]

    companion object {
        private val REASONS = arrayOf(
            "None",
            "Reason: ip banned",
            "Reason: ip reserved",
            "Reason: wrong hexid",
            "Reason: id reserved",
            "Reason: no free ID",
            "Not authed",
            "Reason: already logged in"
        )
    }
}