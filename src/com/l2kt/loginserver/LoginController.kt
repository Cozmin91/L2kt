package com.l2kt.loginserver

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.loginserver.crypt.ScrambledKeyPair
import com.l2kt.loginserver.model.AccountInfo
import com.l2kt.loginserver.model.GameServerInfo
import com.l2kt.loginserver.network.LoginClient
import com.l2kt.loginserver.network.SessionKey
import com.l2kt.loginserver.network.gameserverpackets.ServerStatus
import com.l2kt.loginserver.network.serverpackets.LoginFail
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.RSAKeyGenParameterSpec
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher

object LoginController {

    var clients: MutableMap<String, LoginClient> = ConcurrentHashMap()
    val bannedIps = ConcurrentHashMap<InetAddress, Long>()
    private val failedAttempts = ConcurrentHashMap<InetAddress, Int>()

    private var keyPairs: Array<ScrambledKeyPair?> = arrayOfNulls(10)

    private lateinit var blowfishKeys: Array<ByteArray>

    val LOGGER = CLogger(LoginController::class.java.name)

    private const val USER_INFO_SELECT = "SELECT login, password, access_level, lastServer FROM accounts WHERE login=?"
    private const val AUTOCREATE_ACCOUNTS_INSERT =
        "INSERT INTO accounts (login, password, lastactive, access_level) values (?, ?, ?, ?)"
    private const val ACCOUNT_INFO_UPDATE = "UPDATE accounts SET lastactive = ? WHERE login = ?"
    private const val ACCOUNT_LAST_SERVER_UPDATE = "UPDATE accounts SET lastServer = ? WHERE login = ?"
    private const val ACCOUNT_ACCESS_LEVEL_UPDATE = "UPDATE accounts SET access_level = ? WHERE login = ?"

    /** Time before kicking the client if he didnt logged yet  */
    const val LOGIN_TIMEOUT = 60 * 1000
    private val BLOWFISH_KEYS = 20

    /**
     * @param client the client
     * @param address client host address
     * @param info the account info to checkin
     * @return true when ok to checkin, false otherwise
     */
    private fun canCheckin(client: LoginClient, address: InetAddress, info: AccountInfo): Boolean {
        client.accessLevel = info.accessLevel
        client.lastServer = info.lastServer

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ACCOUNT_INFO_UPDATE).use { ps ->
                    ps.setLong(1, System.currentTimeMillis())
                    ps.setString(2, info.login)
                    ps.execute()

                    return true
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't finish login process.", e)
            return false
        }

    }

    /**
     * @return Returns a random key
     */
    val blowfishKey: ByteArray
        get() = blowfishKeys[(Math.random() * BLOWFISH_KEYS).toInt()]

    /**
     * This method returns one of the cached [ScrambledKeyPairs][ScrambledKeyPair] for communication with Login Clients.
     * @return a scrambled keypair
     */
    val scrambledRSAKeyPair: ScrambledKeyPair
        get() = keyPairs[Rnd.get(10)]!!

    enum class AuthLoginResult {
        INVALID_PASSWORD,
        ACCOUNT_BANNED,
        ALREADY_ON_LS,
        ALREADY_ON_GS,
        AUTH_SUCCESS
    }

    init {

        try {
            val keygen = KeyPairGenerator.getInstance("RSA")
            val spec = RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4)
            keygen.initialize(spec)

            // generate the initial set of keys
            for (i in 0..9)
                keyPairs[i] = ScrambledKeyPair(keygen.generateKeyPair())

            LOGGER.info("Cached 10 KeyPairs for RSA communication.")

            // Test the cipher.
            val rsaCipher = Cipher.getInstance("RSA/ECB/nopadding")
            rsaCipher.init(Cipher.DECRYPT_MODE, keyPairs[0]!!.keyPair.private)

            // Store keys for blowfish communication
            blowfishKeys = Array(BLOWFISH_KEYS) { ByteArray(16) }

            for (i in 0 until BLOWFISH_KEYS) {
                for (j in 0 until blowfishKeys[i].size)
                    blowfishKeys[i][j] = (Rnd.get(255) + 1).toByte()
            }
            LOGGER.info("Stored ${blowfishKeys.size} keys for Blowfish communication.")
        } catch (gse: GeneralSecurityException) {
            LOGGER.error("Failed generating keys.", gse)
        }

        // "Dropping AFK connections on login" task.
        val purge = PurgeThread()
        purge.isDaemon = true
        purge.start()
    }

    fun removeAuthedLoginClient(account: String?) {
        if (account == null)
            return

        clients.remove(account)
    }

    fun getAuthedClient(account: String): LoginClient {
        return clients[account]!!
    }

    /**
     * Update attempts counter. If the maximum amount is reached, it will end with a client ban.
     * @param addr : The InetAddress to test.
     */
    private fun recordFailedAttempt(addr: InetAddress) {
        val attempts = (failedAttempts).merge(addr, 1) { k, v -> k + v }
        if (attempts!! >= Config.LOGIN_TRY_BEFORE_BAN) {
            addBanForAddress(addr, (Config.LOGIN_BLOCK_AFTER_BAN * 1000).toLong())

            // we need to clear the failed login attempts here
            failedAttempts.remove(addr)

            LOGGER.info("IP address: ${addr.hostAddress} has been banned due to too many login attempts.")
        }
    }

    fun retrieveAccountInfo(addr: InetAddress, login: String, password: String): AccountInfo? {
        try {
            val md = MessageDigest.getInstance("SHA")
            val raw = password.toByteArray(StandardCharsets.UTF_8)
            val hashBase64 = Base64.getEncoder().encodeToString(md.digest(raw))

            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(USER_INFO_SELECT).use { ps ->
                    ps.setString(1, login)
                    ps.executeQuery().use { rset ->
                        if (rset.next()) {
                            val info = AccountInfo(
                                rset.getString("login"),
                                rset.getString("password"),
                                rset.getInt("access_level"),
                                rset.getInt("lastServer")
                            )
                            if (!info.checkPassHash(hashBase64)) {
                                // wrong password
                                recordFailedAttempt(addr)
                                return null
                            }

                            failedAttempts.remove(addr)
                            return info
                        }
                    }
                }
            }

            if (!Config.AUTO_CREATE_ACCOUNTS) {
                // account does not exist and auto create account is not desired
                recordFailedAttempt(addr)
                return null
            }

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(AUTOCREATE_ACCOUNTS_INSERT).use { ps ->
                        ps.setString(1, login)
                        ps.setString(2, hashBase64)
                        ps.setLong(3, System.currentTimeMillis())
                        ps.setInt(4, 0)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Exception auto creating account for $login.", e)
                return null
            }

            LOGGER.info("Auto created account '$login'.")
            return retrieveAccountInfo(addr, login, password)
        } catch (e: Exception) {
            LOGGER.error("Exception retrieving account info for '$login'.", e)
            return null
        }

    }

    fun tryCheckinAccount(client: LoginClient, address: InetAddress, info: AccountInfo): AuthLoginResult {
        if (info.accessLevel < 0)
            return AuthLoginResult.ACCOUNT_BANNED

        var ret = AuthLoginResult.INVALID_PASSWORD
        if (canCheckin(client, address, info)) {
            // login was successful, verify presence on Gameservers
            ret = AuthLoginResult.ALREADY_ON_GS
            if (!isAccountInAnyGameServer(info.login)) {
                // account isnt on any GS verify LS itself
                ret = AuthLoginResult.ALREADY_ON_LS

                if ((clients).putIfAbsent(info.login, client) == null)
                    ret = AuthLoginResult.AUTH_SUCCESS
            }
        }
        return ret
    }

    /**
     * Adds the address to the ban list of the login server, with the given duration.
     * @param address The Address to be banned.
     * @param expiration Timestamp in miliseconds when this ban expires
     * @throws UnknownHostException if the address is invalid.
     */
    @Throws(UnknownHostException::class)
    fun addBanForAddress(address: String, expiration: Long) {
        (bannedIps).putIfAbsent(InetAddress.getByName(address), expiration)
    }

    /**
     * Adds the address to the ban list of the login server, with the given duration.
     * @param address The Address to be banned.
     * @param duration is miliseconds
     */
    fun addBanForAddress(address: InetAddress, duration: Long) {
        (bannedIps).putIfAbsent(address, System.currentTimeMillis() + duration)
    }

    fun isBannedAddress(address: InetAddress): Boolean {
        val time = bannedIps[address]
        if (time != null) {
            if (time > 0 && time < System.currentTimeMillis()) {
                bannedIps.remove(address)
                LOGGER.info("Removed expired ip address ban ${address.hostAddress}.")
                return false
            }
            return true
        }
        return false
    }

    /**
     * Remove the specified address from the ban list
     * @param address The address to be removed from the ban list
     * @return true if the ban was removed, false if there was no ban for this ip
     */
    fun removeBanForAddress(address: InetAddress): Boolean {
        return bannedIps.remove(address) != null
    }

    /**
     * Remove the specified address from the ban list
     * @param address The address to be removed from the ban list
     * @return true if the ban was removed, false if there was no ban for this ip or the address was invalid.
     */
    fun removeBanForAddress(address: String): Boolean {
        try {
            return this.removeBanForAddress(InetAddress.getByName(address))
        } catch (e: UnknownHostException) {
            return false
        }

    }

    fun getKeyForAccount(account: String): SessionKey? {
        val client = clients[account]
        return client?.sessionKey
    }

    fun isAccountInAnyGameServer(account: String): Boolean {
        for (gsi in GameServerManager.registeredGameServers.values) {
            val gst = gsi.gameServerThread
            if (gst != null && gst.hasAccountOnGameServer(account))
                return true
        }
        return false
    }

    fun getAccountOnGameServer(account: String): GameServerInfo? {
        for (gsi in GameServerManager.registeredGameServers.values) {
            val gst = gsi.gameServerThread
            if (gst != null && gst.hasAccountOnGameServer(account))
                return gsi
        }
        return null
    }

    fun isLoginPossible(client: LoginClient, serverId: Int): Boolean {
        val gsi = GameServerManager.registeredGameServers[serverId]
        if (gsi == null || !gsi.isAuthed)
            return false

        val loginOk =
            gsi.currentPlayerCount < gsi.maxPlayers && gsi.status != ServerStatus.STATUS_GM_ONLY || client.accessLevel > 0

        if (loginOk && client.lastServer != serverId) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(ACCOUNT_LAST_SERVER_UPDATE).use { ps ->
                        ps.setInt(1, serverId)
                        ps.setString(2, client.account)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't set lastServer.", e)
            }

        }
        return loginOk
    }

    fun setAccountAccessLevel(account: String, banLevel: Int) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ACCOUNT_ACCESS_LEVEL_UPDATE).use { ps ->
                    ps.setInt(1, banLevel)
                    ps.setString(2, account)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't set access level $banLevel for $account.", e)
        }

    }

    private class PurgeThread : Thread() {
        init {
            name = "PurgeThread"
        }

        override fun run() {
            while (!isInterrupted) {
                for (client in clients.values) {
                    if (client.connectionStartTime + LOGIN_TIMEOUT < System.currentTimeMillis())
                        client.close(LoginFail.REASON_ACCESS_FAILED)
                }

                try {
                    Thread.sleep((LOGIN_TIMEOUT / 2).toLong())
                } catch (e: InterruptedException) {
                    return
                }

            }
        }
    }
}