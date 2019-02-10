package com.l2kt.loginserver.network

import com.l2kt.commons.mmocore.IPacketHandler
import com.l2kt.commons.mmocore.ReceivablePacket
import com.l2kt.loginserver.network.LoginClient.LoginClientState
import com.l2kt.loginserver.network.clientpackets.AuthGameGuard
import com.l2kt.loginserver.network.clientpackets.RequestAuthLogin
import com.l2kt.loginserver.network.clientpackets.RequestServerList
import com.l2kt.loginserver.network.clientpackets.RequestServerLogin
import java.nio.ByteBuffer

/**
 * Handler for packets received by Login Server
 */
class LoginPacketHandler : IPacketHandler<LoginClient> {
    override fun handlePacket(buf: ByteBuffer, client: LoginClient): ReceivablePacket<LoginClient>? {
        val opcode = buf.get().toInt() and 0xFF

        var packet: ReceivablePacket<LoginClient>? = null
        val state = client.state

        when (state) {
            LoginClient.LoginClientState.CONNECTED -> if (opcode == 0x07)
                packet = AuthGameGuard()
            else
                debugOpcode(opcode, state)

            LoginClient.LoginClientState.AUTHED_GG -> if (opcode == 0x00)
                packet = RequestAuthLogin()
            else
                debugOpcode(opcode, state)

            LoginClient.LoginClientState.AUTHED_LOGIN -> when (opcode) {
                0x05 -> packet = RequestServerList()
                0x02 -> packet = RequestServerLogin()
                else -> debugOpcode(opcode, state)
            }
        }
        return packet
    }

    private fun debugOpcode(opcode: Int, state: LoginClientState) {
        println("Unknown Opcode: " + opcode + " for state: " + state.name)
    }
}