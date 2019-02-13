package com.l2kt.loginserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.loginserver.LoginController
import com.l2kt.loginserver.network.LoginClient
import com.l2kt.loginserver.network.SessionKey
import com.l2kt.loginserver.network.serverpackets.AccountKicked
import com.l2kt.loginserver.network.serverpackets.AccountKicked.AccountKickedReason
import com.l2kt.loginserver.network.serverpackets.LoginFail
import com.l2kt.loginserver.network.serverpackets.LoginOk
import com.l2kt.loginserver.network.serverpackets.ServerList
import java.security.GeneralSecurityException
import javax.crypto.Cipher

class RequestAuthLogin : L2LoginClientPacket() {
    private val _raw = ByteArray(128)

    var user: String = ""
        private set
    var password: String = ""
        private set
    var oneTimePassword: Int = 0
        private set

    public override fun readImpl(): Boolean {
        if (super._buf.remaining() >= 128) {
            readB(_raw)
            return true
        }
        return false
    }

    override fun run() {
        val decrypted: ByteArray
        val client = client
        try {
            val rsaCipher = Cipher.getInstance("RSA/ECB/nopadding")
            rsaCipher.init(Cipher.DECRYPT_MODE, client.rsaPrivateKey)
            decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80)
        } catch (e: GeneralSecurityException) {
            L2LoginClientPacket.LOGGER.error("Failed to generate a cipher.", e)
            return
        }

        try {
            user = String(decrypted, 0x5E, 14).trim{ it <= ' ' }.toLowerCase()
            password = String(decrypted, 0x6C, 16).trim{ it <= ' ' }
            oneTimePassword = decrypted[0x7c].toInt()
            oneTimePassword = oneTimePassword or (decrypted[0x7d].toInt() shl 8)
            oneTimePassword = oneTimePassword or (decrypted[0x7e].toInt() shl 16)
            oneTimePassword = oneTimePassword or (decrypted[0x7f].toInt() shl 24)
        } catch (e: Exception) {
            L2LoginClientPacket.LOGGER.error("Failed to decrypt user/password.", e)
            return
        }

        val clientAddr = client.connection.inetAddress

        val info = LoginController.retrieveAccountInfo(clientAddr, user, password)
        if (info == null) {
            client.close(LoginFail.REASON_USER_OR_PASS_WRONG)
            return
        }

        val result = LoginController.tryCheckinAccount(client, clientAddr, info)
        when (result) {
            LoginController.AuthLoginResult.AUTH_SUCCESS -> {
                client.account = info.login
                client.state = LoginClient.LoginClientState.AUTHED_LOGIN
                client.sessionKey = SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt())
                client.sendPacket(if (Config.SHOW_LICENCE) LoginOk(client.sessionKey!!) else ServerList(client))
            }

            LoginController.AuthLoginResult.INVALID_PASSWORD -> client.close(LoginFail.REASON_USER_OR_PASS_WRONG)

            LoginController.AuthLoginResult.ACCOUNT_BANNED -> client.close(AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED))

            LoginController.AuthLoginResult.ALREADY_ON_LS -> {
                val oldClient = LoginController.getAuthedClient(info.login)
                oldClient.close(LoginFail.REASON_ACCOUNT_IN_USE)
                LoginController.removeAuthedLoginClient(info.login)
                client.close(LoginFail.REASON_ACCOUNT_IN_USE)
            }

            LoginController.AuthLoginResult.ALREADY_ON_GS -> {
                val gsi = LoginController.getAccountOnGameServer(info.login)
                if (gsi != null) {
                    client.close(LoginFail.REASON_ACCOUNT_IN_USE)

                    if (gsi.isAuthed)
                        gsi.gameServerThread!!.kickPlayer(info.login)
                }
            }
        }
    }
}