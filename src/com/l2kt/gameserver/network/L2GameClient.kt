package com.l2kt.gameserver.network

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.mmocore.MMOClient
import com.l2kt.commons.mmocore.MMOConnection
import com.l2kt.commons.mmocore.ReceivablePacket
import com.l2kt.gameserver.LoginServerThread
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.CharSelectSlot
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.network.serverpackets.ServerClose
import java.nio.ByteBuffer
import java.sql.PreparedStatement
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Represents a client connected on Game Server
 * @author KenM
 */
class L2GameClient(con: MMOConnection<L2GameClient>) : MMOClient<MMOConnection<L2GameClient>>(con), Runnable {

    var _state: GameClientState

    // Info
    var accountName: String? = null
    var sessionId: SessionKey? = null
    var activeChar: Player? = null
    val activeCharLock = ReentrantLock()

    private var _isAuthedGG: Boolean = false
    val connectionStartTime: Long
    private var _slots: Array<CharSelectSlot>? = null

    // floodprotectors
    val floodProtectors = LongArray(FloodProtectors.Action.VALUES_LENGTH)

    // Task
    protected val _autoSaveInDB: ScheduledFuture<*>?
    protected var _cleanupTask: ScheduledFuture<*>? = null

    var _crypt: GameCrypt
    val stats: ClientStats

    var isDetached = false

    private val _packetQueue: ArrayBlockingQueue<ReceivablePacket<L2GameClient>>
    private val _queueLock = ReentrantLock()

    var state: GameClientState
        get() = _state
        set(pState) {
            if (_state != pState) {
                _state = pState
                _packetQueue.clear()
            }
        }

    enum class GameClientState {
        CONNECTED, // client has just connected
        AUTHED, // client has authed but doesnt has character attached to it yet
        IN_GAME // client has selected a char and is in game
    }

    init {
        _state = GameClientState.CONNECTED
        connectionStartTime = System.currentTimeMillis()
        _crypt = GameCrypt()
        stats = ClientStats()
        _packetQueue = ArrayBlockingQueue(Config.CLIENT_PACKET_QUEUE_SIZE)

        _autoSaveInDB = ThreadPool.scheduleAtFixedRate(AutoSaveTask(), 300000L, 900000L)
    }

    fun enableCrypt(): ByteArray {
        val key = BlowFishKeygen.randomKey
        _crypt.setKey(key)
        return key
    }

    override fun decrypt(buf: ByteBuffer, size: Int): Boolean {
        _crypt.decrypt(buf.array(), buf.position(), size)
        return true
    }

    override fun encrypt(buf: ByteBuffer, size: Int): Boolean {
        _crypt.encrypt(buf.array(), buf.position(), size)
        buf.position(buf.position() + size)
        return true
    }

    fun setGameGuardOk(`val`: Boolean) {
        _isAuthedGG = `val`
    }

    fun sendPacket(gsp: L2GameServerPacket) {
        if (isDetached)
            return

        connection.sendPacket(gsp)
        gsp.runImpl()
    }

    /**
     * Method to handle character deletion
     * @param charslot The slot to check.
     * @return a byte:
     *  * -1: Error: No char was found for such charslot, caught exception, etc...
     *  * 0: character is not member of any clan, proceed with deletion
     *  * 1: character is member of a clan, but not clan leader
     *  * 2: character is clan leader
     */
    fun markToDeleteChar(charslot: Int): Byte {
        val objid = getObjectIdForSlot(charslot)

        if (objid < 0)
            return -1

        try {
            L2DatabaseFactory.connection.use { con ->
                var statement = con.prepareStatement("SELECT clanId FROM characters WHERE obj_id=?")
                statement.setInt(1, objid)
                val rs = statement.executeQuery()

                rs.next()

                val clanId = rs.getInt(1)
                var answer: Byte = 0
                if (clanId != 0) {
                    val clan = ClanTable.getInstance().getClan(clanId)

                    if (clan == null)
                        answer = 0 // jeezes!
                    else if (clan.leaderId == objid)
                        answer = 2
                    else
                        answer = 1
                }

                rs.close()
                statement.close()

                // Setting delete time
                if (answer.toInt() == 0) {
                    if (Config.DELETE_DAYS == 0)
                        deleteCharByObjId(objid)
                    else {
                        statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?")
                        statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L)
                        statement.setInt(2, objid)
                        statement.execute()
                        statement.close()
                    }
                }

                return answer
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Error updating delete time of character.", e)
            return -1
        }

    }

    fun markRestoredChar(charslot: Int) {
        val objid = getObjectIdForSlot(charslot)
        if (objid < 0)
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?")
                statement.setInt(1, objid)
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "Error restoring character.", e)
        }

    }

    fun loadCharFromDisk(slot: Int): Player? {
        val objectId = getObjectIdForSlot(slot)
        if (objectId < 0)
            return null

        var player: Player? = World.getInstance().getPlayer(objectId)
        if (player != null) {
            // exploit prevention, should not happens in normal way
            _log.severe("Attempt of double login: " + player.name + "(" + objectId + ") " + accountName)

            if (player.client != null)
                player.client.closeNow()
            else
                player.deleteMe()

            return null
        }

        player = Player.restore(objectId)
        if (player != null) {
            player.setRunning() // running is default
            player.standUp() // standing is default

            player.setOnlineStatus(true, false)
            World.getInstance().addPlayer(player)
        } else
            _log.severe("L2GameClient: could not restore in slot: $slot")

        return player
    }

    /**
     * Get a [CharSelectSlot] based on its id. Integrity checks are included.
     * @param id : The slot id to call.
     * @return the associated slot informations based on slot id.
     */
    fun getCharSelectSlot(id: Int): CharSelectSlot? {
        return if (_slots == null || id < 0 || id >= _slots!!.size) null else _slots!![id]

    }

    /**
     * Set the character selection slots.
     * @param list : Use the List as character slots.
     */
    fun setCharSelectSlot(list: Array<CharSelectSlot>) {
        _slots = list
    }

    fun close(gsp: L2GameServerPacket) {
        connection.close(gsp)
    }

    /**
     * @param charslot
     * @return
     */
    private fun getObjectIdForSlot(charslot: Int): Int {
        val info = getCharSelectSlot(charslot)
        if (info == null) {
            _log.warning(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.")
            return -1
        }
        return info.objectId
    }

    override fun onForcedDisconnection() {
        _log.fine("Client " + toString() + " disconnected abnormally.")
    }

    override fun onDisconnection() {
        // no long running tasks here, do it async
        try {
            ThreadPool.execute(DisconnectTask())
        } catch (e: RejectedExecutionException) {
            // server is closing
        }

    }

    /**
     * Close client connection with [ServerClose] packet
     */
    fun closeNow() {
        isDetached = true // prevents more packets execution
        close(ServerClose.STATIC_PACKET)
        synchronized(this) {
            if (_cleanupTask != null)
                cancelCleanup()

            _cleanupTask = ThreadPool.schedule(CleanupTask(), 0) // instant
        }
    }

    /**
     * Produces the best possible string representation of this client.
     */
    override fun toString(): String {
        return try {
            val address = connection.inetAddress
            when (state) {
                L2GameClient.GameClientState.CONNECTED -> "[IP: " + (if (address == null) "disconnected" else address.hostAddress) + "]"
                L2GameClient.GameClientState.AUTHED -> "[Account: " + accountName + " - IP: " + (if (address == null) "disconnected" else address.hostAddress) + "]"
                L2GameClient.GameClientState.IN_GAME -> "[Character: " + (if (activeChar == null) "disconnected" else activeChar!!.name) + " - Account: " + accountName + " - IP: " + (if (address == null) "disconnected" else address.hostAddress) + "]"
                else -> throw IllegalStateException("Missing state on switch")
            }
        } catch (e: NullPointerException) {
            "[Character read failed due to disconnect]"
        }

    }

    protected inner class DisconnectTask : Runnable {
        /**
         * @see java.lang.Runnable.run
         */
        override fun run() {
            var fast = true

            try {
                if (activeChar != null && !isDetached) {
                    isDetached = true
                    fast = !activeChar!!.isInCombat && !activeChar!!.isLocked
                }
                cleanMe(fast)
            } catch (e1: Exception) {
                _log.log(Level.WARNING, "error while disconnecting client", e1)
            }

        }
    }

    fun cleanMe(fast: Boolean) {
        try {
            synchronized(this) {
                if (_cleanupTask == null)
                    _cleanupTask = ThreadPool.schedule(CleanupTask(), if (fast) 5 else 15000L)
            }
        } catch (e1: Exception) {
            _log.log(Level.WARNING, "Error during cleanup.", e1)
        }

    }

    protected inner class CleanupTask : Runnable {
        /**
         * @see java.lang.Runnable.run
         */
        override fun run() {
            try {
                // we are going to manually save the char below thus we can force the cancel
                _autoSaveInDB?.cancel(true)

                if (activeChar != null)
                // this should only happen on connection loss
                {
                    if (activeChar!!.isLocked)
                        _log.log(
                            Level.WARNING,
                            activeChar!!.name + " is still performing subclass actions during disconnect."
                        )

                    // prevent closing again
                    activeChar!!.client = null

                    if (activeChar!!.isOnline)
                        activeChar!!.deleteMe()
                }
                activeChar = null
            } catch (e1: Exception) {
                _log.log(Level.WARNING, "Error while cleanup client.", e1)
            } finally {
                LoginServerThread.sendLogout(accountName)
            }
        }
    }

    protected inner class AutoSaveTask : Runnable {
        override fun run() {
            try {
                if (activeChar != null && activeChar!!.isOnline) {
                    activeChar!!.store()

                    if (activeChar!!.pet != null)
                        activeChar!!.pet!!.store()
                }
            } catch (e: Exception) {
                _log.log(Level.SEVERE, "Error on AutoSaveTask.", e)
            }

        }
    }

    /**
     * @return false if client can receive packets. True if detached, or flood detected, or queue overflow detected and queue still not empty.
     */
    fun dropPacket(): Boolean {
        if (isDetached)
        // detached clients can't receive any packets
            return true

        // flood protection
        if (stats.countPacket(_packetQueue.size)) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return true
        }

        return stats.dropPacket()
    }

    /**
     * Counts buffer underflow exceptions.
     */
    fun onBufferUnderflow() {
        if (stats.countUnderflowException()) {
            _log.severe("Client " + toString() + " - Disconnected: Too many buffer underflow exceptions.")
            closeNow()
            return
        }
        if (_state == GameClientState.CONNECTED)
        // in CONNECTED state kick client immediately
        {
            if (Config.PACKET_HANDLER_DEBUG)
                _log.severe("Client " + toString() + " - Disconnected, too many buffer underflows in non-authed state.")
            closeNow()
        }
    }

    /**
     * Counts unknown packets
     */
    fun onUnknownPacket() {
        if (stats.countUnknownPacket()) {
            _log.severe("Client " + toString() + " - Disconnected: Too many unknown packets.")
            closeNow()
            return
        }
        if (_state == GameClientState.CONNECTED)
        // in CONNECTED state kick client immediately
        {
            if (Config.PACKET_HANDLER_DEBUG)
                _log.severe("Client " + toString() + " - Disconnected, too many unknown packets in non-authed state.")
            closeNow()
        }
    }

    /**
     * Add packet to the queue and start worker thread if needed
     * @param packet The packet to execute.
     */
    fun execute(packet: ReceivablePacket<L2GameClient>) {
        if (stats.countFloods()) {
            _log.severe("Client " + toString() + " - Disconnected, too many floods:" + stats.longFloods + " long and " + stats.shortFloods + " short.")
            closeNow()
            return
        }

        if (!_packetQueue.offer(packet)) {
            if (stats.countQueueOverflow()) {
                _log.severe("Client " + toString() + " - Disconnected, too many queue overflows.")
                closeNow()
            } else
                sendPacket(ActionFailed.STATIC_PACKET)

            return
        }

        if (_queueLock.isLocked)
        // already processing
            return

        try {
            if (_state == GameClientState.CONNECTED && stats.processedPackets > 3) {
                if (Config.PACKET_HANDLER_DEBUG)
                    _log.severe("Client " + toString() + " - Disconnected, too many packets in non-authed state.")

                closeNow()
                return
            }

            ThreadPool.execute(this)
        } catch (e: RejectedExecutionException) {
        }

    }

    override fun run() {
        if (!_queueLock.tryLock())
            return

        try {
            var count = 0
            var packet: ReceivablePacket<L2GameClient>?
            while (true) {
                packet = _packetQueue.poll()
                if (packet == null)
                // queue is empty
                    return

                if (isDetached)
                // clear queue immediately after detach
                {
                    _packetQueue.clear()
                    return
                }

                try {
                    packet.run()
                } catch (e: Exception) {
                    _log.severe("Exception during execution " + packet.javaClass.simpleName + ", client: " + toString() + "," + e.message)
                }

                count++
                if (stats.countBurst(count))
                    return
            }
        } finally {
            _queueLock.unlock()
        }
    }

    private fun cancelCleanup(): Boolean {
        val task = _cleanupTask
        if (task != null) {
            _cleanupTask = null
            return task.cancel(true)
        }
        return false
    }

    companion object {
        protected val _log = Logger.getLogger(L2GameClient::class.java.name)

        fun deleteCharByObjId(objid: Int) {
            if (objid < 0)
                return

            PlayerInfoTable.getInstance().removePlayer(objid)

            try {
                L2DatabaseFactory.connection.use { con ->
                    var statement: PreparedStatement

                    statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?")
                    statement.setInt(1, objid)
                    statement.setInt(2, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_memo WHERE charId=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement =
                        con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement =
                        con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM character_raid_points WHERE char_id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()

                    statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?")
                    statement.setInt(1, objid)
                    statement.execute()
                    statement.close()
                }
            } catch (e: Exception) {
                _log.log(Level.SEVERE, "Error deleting character.", e)
            }

        }
    }
}