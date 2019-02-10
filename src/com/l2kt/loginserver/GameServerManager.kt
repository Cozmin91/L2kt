package com.l2kt.loginserver

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.loginserver.GameServerManager.forEach
import com.l2kt.loginserver.model.GameServerInfo
import org.w3c.dom.Document
import java.math.BigInteger
import java.nio.file.Path
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.RSAKeyGenParameterSpec
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object GameServerManager : IXmlReader {

    val serverNames = HashMap<Int, String>()
    val registeredGameServers = ConcurrentHashMap<Int, GameServerInfo>()
    private const val KEYS_SIZE = 10
    private var keyPairs: Array<KeyPair?> = arrayOfNulls(KEYS_SIZE)
    private val LOGGER = CLogger(GameServerManager::class.java.name)

    private const val LOAD_SERVERS = "SELECT * FROM gameservers"
    private const val ADD_SERVER = "INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)"

    private fun stringToHex(string: String): ByteArray {
        return BigInteger(string, 16).toByteArray()
    }

    private fun hexToString(hex: ByteArray?): String {
        return if (hex == null) "null" else BigInteger(hex).toString(16)
    }

    val keyPair: KeyPair
        get() = keyPairs[Rnd.get(10)]!!

    init {
        load()
    }

    override fun load() {
        parseFile("serverNames.xml")
        LOGGER.info("Loaded {} server names.", serverNames.size)

        loadRegisteredGameServers()
        LOGGER.info("Loaded {} registered gameserver(s).", registeredGameServers.size)

        initRSAKeys()
        LOGGER.info("Cached {} RSA keys for gameserver communication.", keyPairs.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "server") { serverNode ->
                val set = parseAttributes(serverNode)
                serverNames[set.getInteger("id")] = set.getString("name")
            }
        }
    }

    private fun initRSAKeys() {
        try {
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4))

            for (i in 0 until KEYS_SIZE)
                keyPairs[i] = keyGen.genKeyPair()
        } catch (e: Exception) {
            LOGGER.error("Error loading RSA keys for Game Server communication.", e)
        }

    }

    private fun loadRegisteredGameServers() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_SERVERS).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val id = rs.getInt("server_id")
                            registeredGameServers[id] = GameServerInfo(id, stringToHex(rs.getString("hexid")))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error loading registered gameservers.", e)
        }

    }

    fun registerWithFirstAvailableId(gsi: GameServerInfo): Boolean {
        for (id in serverNames.keys) {
            if (!registeredGameServers.containsKey(id)) {
                registeredGameServers[id] = gsi
                gsi.id = id
                return true
            }
        }
        return false
    }

    fun register(id: Int, gsi: GameServerInfo): Boolean {
        if (!registeredGameServers.containsKey(id)) {
            registeredGameServers[id] = gsi
            gsi.id = id
            return true
        }
        return false
    }

    fun registerServerOnDB(gsi: GameServerInfo) {
        registerServerOnDB(gsi.hexId, gsi.id, gsi.hostName!!)
    }

    fun registerServerOnDB(hexId: ByteArray, id: Int, hostName: String) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ADD_SERVER).use { ps ->
                    ps.setString(1, hexToString(hexId))
                    ps.setInt(2, id)
                    ps.setString(3, hostName)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while saving gameserver data.", e)
        }
    }
}