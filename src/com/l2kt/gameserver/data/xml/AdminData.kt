package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.AdminData.forEach
import com.l2kt.gameserver.model.AccessLevel
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * This class loads and handles following concepts :
 *
 *  * [AccessLevel]s retain informations such as isGM() and multiple allowed actions.
 *  * Admin command rights' authorized [AccessLevel].
 *  * GM list holds GM [Player]s used by /gmlist. It also stores the hidden state.
 *
 */
object AdminData : IXmlReader {
    private val _accessLevels = TreeMap<Int, AccessLevel>()
    private val _adminCommandAccessRights = HashMap<String, Int>()
    private val _gmList = ConcurrentHashMap<Player, Boolean>()

    /**
     * @return the master [AccessLevel] level. It is always the AccessLevel with the highest level.
     */
    val masterAccessLevel: Int
        get() = if (_accessLevels.isEmpty()) 0 else _accessLevels.lastKey()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/accessLevels.xml")
        IXmlReader.LOGGER.info("Loaded {} access levels.", _accessLevels.size)

        parseFile("./data/xml/adminCommands.xml")
        IXmlReader.LOGGER.info("Loaded {} admin command rights.", _adminCommandAccessRights.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "access") { accessNode ->
                val set = parseAttributes(accessNode)
                _accessLevels[set.getInteger("level")] = AccessLevel(set)
            }
            forEach(listNode, "aCar") { aCarNode ->
                val set = parseAttributes(aCarNode)
                _adminCommandAccessRights[set.getString("name")] = set.getInteger("accessLevel")
            }
        }
    }

    fun reload() {
        _accessLevels.clear()
        _adminCommandAccessRights.clear()

        load()
    }

    /**
     * @param level : The level to check.
     * @return the [AccessLevel] based on its level.
     */
    fun getAccessLevel(level: Int): AccessLevel? {
        return _accessLevels[if (level < 0) -1 else level]
    }

    /**
     * @param level : The level to check.
     * @return true if an [AccessLevel] exists.
     */
    fun hasAccessLevel(level: Int): Boolean {
        return _accessLevels.containsKey(level)
    }

    /**
     * @param command : The admin command to check.
     * @param accessToCheck : The [AccessLevel] to check.
     * @return true if an AccessLevel can use a specific command (set as parameter).
     */
    fun hasAccess(command: String, accessToCheck: AccessLevel): Boolean {
        val level = _adminCommandAccessRights[command]
        if (level == null) {
            IXmlReader.LOGGER.warn("No rights defined for admin command '{}'.", command)
            return false
        }

        val access = getAccessLevel(level)
        return access != null && (access.level == accessToCheck.level || accessToCheck.hasChildAccess(access))
    }

    /**
     * @param includeHidden : If true, we add hidden GMs.
     * @return the List of GM [Player]s. This List can include or not hidden GMs.
     */
    fun getAllGms(includeHidden: Boolean): List<Player> {
        val list = ArrayList<Player>()
        for ((key, value) in _gmList) {
            if (includeHidden || !value)
                list.add(key)
        }
        return list
    }

    /**
     * @param includeHidden : If true, we add hidden GMs.
     * @return the List of GM [Player]s names. This List can include or not hidden GMs.
     */
    fun getAllGmNames(includeHidden: Boolean): List<String> {
        val list = ArrayList<String>()
        for ((key, value) in _gmList) {
            if (!value)
                list.add(key.name)
            else if (includeHidden)
                list.add(key.name + " (invis)")
        }
        return list
    }

    /**
     * Add a [Player] to the _gmList map.
     * @param player : The Player to add on the map.
     * @param hidden : The hidden state of this Player.
     */
    fun addGm(player: Player, hidden: Boolean) {
        _gmList[player] = hidden
    }

    /**
     * Delete a [Player] from the _gmList map..
     * @param player : The Player to remove from the map.
     */
    fun deleteGm(player: Player) {
        _gmList.remove(player)
    }

    /**
     * Refresh hidden state for a GM [Player].
     * @param player : The GM to affect.
     * @return the current GM state.
     */
    fun showOrHideGm(player: Player): Boolean {
        return _gmList.computeIfPresent(player) { k, v -> !v } ?: false
    }

    /**
     * @param includeHidden : Include or not hidden GM Players.
     * @return true if at least one GM [Player] is online.
     */
    fun isGmOnline(includeHidden: Boolean): Boolean {
        for ((_, value) in _gmList) {
            if (includeHidden || !value)
                return true
        }
        return false
    }

    /**
     * @param player : The player to test.
     * @return true if this [Player] is registered as GM.
     */
    fun isRegisteredAsGM(player: Player): Boolean {
        return _gmList.containsKey(player)
    }

    /**
     * Send the GM list of current online GM [Player]s to the Player set as parameter.
     * @param player : The Player to send list.
     */
    fun sendListToPlayer(player: Player) {
        if (isGmOnline(player.isGM)) {
            player.sendPacket(SystemMessageId.GM_LIST)

            for (name in getAllGmNames(player.isGM))
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GM_S1).addString(name))
        } else {
            player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW)
            player.sendPacket(PlaySound("systemmsg_e.702"))
        }
    }

    /**
     * Broadcast to GM [Player]s a specific packet set as parameter.
     * @param packet : The [L2GameServerPacket] packet to broadcast.
     */
    fun broadcastToGMs(packet: L2GameServerPacket) {
        for (gm in getAllGms(true))
            gm.sendPacket(packet)
    }

    /**
     * Broadcast a message to GM [Player]s.
     * @param message : The String message to broadcast.
     */
    fun broadcastMessageToGMs(message: String) {
        for (gm in getAllGms(true))
            gm.sendMessage(message)
    }
}