package com.l2kt.loginserver.network

import com.l2kt.commons.mmocore.MMOClient
import com.l2kt.commons.mmocore.MMOConnection
import com.l2kt.commons.mmocore.SendablePacket
import com.l2kt.commons.random.Rnd
import com.l2kt.loginserver.LoginController
import com.l2kt.loginserver.crypt.LoginCrypt
import com.l2kt.loginserver.crypt.ScrambledKeyPair
import com.l2kt.loginserver.network.serverpackets.L2LoginServerPacket
import com.l2kt.loginserver.network.serverpackets.LoginFail
import com.l2kt.loginserver.network.serverpackets.PlayFail
import java.io.IOException
import java.nio.ByteBuffer
import java.security.interfaces.RSAPrivateKey
import java.util.logging.Logger

/**
 * Represents a client connected into the LoginServer
 */
class LoginClient(con: MMOConnection<LoginClient>) : MMOClient<MMOConnection<LoginClient>>(con) {

    var state: LoginClientState? = null

    private val _loginCrypt: LoginCrypt
    private val _scrambledPair: ScrambledKeyPair
    val blowfishKey: ByteArray

    var account: String? = null
    var accessLevel: Int = 0
    var lastServer: Int = 0
    var sessionKey: SessionKey? = null
    val sessionId: Int
    private var _joinedGS: Boolean = false

    val connectionStartTime: Long

    val scrambledModulus: ByteArray
        get() = _scrambledPair.scrambledModulus

    val rsaPrivateKey: RSAPrivateKey
        get() = _scrambledPair.keyPair.private as RSAPrivateKey

    enum class LoginClientState {
        CONNECTED,
        AUTHED_GG,
        AUTHED_LOGIN
    }

    init {

        state = LoginClientState.CONNECTED
        _scrambledPair = LoginController.scrambledRSAKeyPair
        blowfishKey = LoginController.blowfishKey
        sessionId = Rnd.nextInt()
        connectionStartTime = System.currentTimeMillis()
        _loginCrypt = LoginCrypt()
        _loginCrypt.setKey(blowfishKey)
    }

    override fun decrypt(buf: ByteBuffer, size: Int): Boolean {
        try {
            if (!_loginCrypt.decrypt(buf.array(), buf.position(), size)) {
                _log.warning("Wrong checksum from client: " + toString())
                super.getConnection().close(null as SendablePacket<LoginClient>?)
                return false
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            super.getConnection().close(null as SendablePacket<LoginClient>?)
            return false
        }

    }

    override fun encrypt(buf: ByteBuffer, size: Int): Boolean {
        var size = size
        val offset = buf.position()
        try {
            size = _loginCrypt.encrypt(buf.array(), offset, size)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        buf.position(offset + size)
        return true
    }

    fun hasJoinedGS(): Boolean {
        return _joinedGS
    }

    fun setJoinedGS(`val`: Boolean) {
        _joinedGS = `val`
    }

    fun sendPacket(lsp: L2LoginServerPacket) {
        connection.sendPacket(lsp)
    }

    fun close(reason: LoginFail) {
        connection.close(reason)
    }

    fun close(reason: PlayFail) {
        connection.close(reason)
    }

    fun close(lsp: L2LoginServerPacket) {
        connection.close(lsp)
    }

    public override fun onDisconnection() {
        if (!hasJoinedGS() || connectionStartTime + LoginController.LOGIN_TIMEOUT < System.currentTimeMillis())
            LoginController.removeAuthedLoginClient(account)
    }

    override fun toString(): String {
        val address = connection.inetAddress
        return if (state == LoginClientState.AUTHED_LOGIN) "[" + account + " (" + (if (address == null) "disconnected" else address.hostAddress) + ")]" else "[" + (if (address == null) "disconnected" else address.hostAddress) + "]"

    }

    override fun onForcedDisconnection() {}

    companion object {
        private val _log = Logger.getLogger(LoginClient::class.java.name)
    }
}