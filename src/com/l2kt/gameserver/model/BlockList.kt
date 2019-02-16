package com.l2kt.gameserver.model

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.sql.PreparedStatement
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class BlockList(private val _owner: Player) {
    private var _blockList: MutableList<Int>? = mutableListOf()

    private var isBlockAll: Boolean
        get() = _owner.isInRefusalMode
        set(state) {
            _owner.isInRefusalMode = state
        }

    val blockList: MutableList<Int>?
        get() = _blockList

    init {
        _blockList = _offlineList[_owner.objectId]
        if (_blockList == null)
            _blockList = loadList(_owner.objectId)
    }

    @Synchronized
    private fun addToBlockList(target: Int) {
        _blockList!!.add(target)
        updateInDB(target, true)
    }

    @Synchronized
    private fun removeFromBlockList(target: Int) {
        _blockList!!.remove(Integer.valueOf(target))
        updateInDB(target, false)
    }

    fun playerLogout() {
        _offlineList[_owner.objectId] = _blockList
    }

    private fun updateInDB(targetId: Int, state: Boolean) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement: PreparedStatement

                if (state) {
                    statement =
                            con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, relation) VALUES (?, ?, 1)")
                    statement.setInt(1, _owner.objectId)
                    statement.setInt(2, targetId)
                } else {
                    statement =
                            con.prepareStatement("DELETE FROM character_friends WHERE char_id = ? AND friend_id = ? AND relation = 1")
                    statement.setInt(1, _owner.objectId)
                    statement.setInt(2, targetId)
                }
                statement.execute()
                statement.close()
            }
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Could not add/remove block player: " + e.message, e)
        }

    }

    fun isInBlockList(target: Player): Boolean {
        return _blockList!!.contains(target.objectId)
    }

    fun isInBlockList(targetId: Int): Boolean {
        return _blockList!!.contains(targetId)
    }

    fun isBlockAll(listOwner: Player): Boolean {
        return listOwner.blockList.isBlockAll
    }

    companion object {
        private val _log = Logger.getLogger(BlockList::class.java.name)
        private val _offlineList = HashMap<Int, MutableList<Int>?>()

        private fun loadList(ObjId: Int): MutableList<Int> {
            val list = ArrayList<Int>()

            try {
                L2DatabaseFactory.connection.use { con ->
                    val statement =
                        con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 1")
                    statement.setInt(1, ObjId)
                    val rset = statement.executeQuery()

                    var friendId: Int
                    while (rset.next()) {
                        friendId = rset.getInt("friend_id")
                        if (friendId == ObjId)
                            continue

                        list.add(friendId)
                    }

                    rset.close()
                    statement.close()
                }
            } catch (e: Exception) {
                _log.log(
                    Level.WARNING,
                    "Error found in " + ObjId + " friendlist while loading BlockList: " + e.message,
                    e
                )
            }

            return list
        }

        fun isBlocked(listOwner: Player, target: Player): Boolean {
            val blockList = listOwner.blockList
            return blockList.isBlockAll || blockList.isInBlockList(target)
        }

        fun isBlocked(listOwner: Player, targetId: Int): Boolean {
            val blockList = listOwner.blockList
            return blockList.isBlockAll || blockList.isInBlockList(targetId)
        }

        fun addToBlockList(listOwner: Player?, targetId: Int) {
            if (listOwner == null)
                return

            val charName = PlayerInfoTable.getPlayerName(targetId)

            if (listOwner.friendList.contains(targetId)) {
                val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST)
                sm.addString(charName!!)
                listOwner.sendPacket(sm)
                return
            }

            if (listOwner.blockList.blockList!!.contains(targetId)) {
                listOwner.sendMessage("Already in ignore list.")
                return
            }

            listOwner.blockList.addToBlockList(targetId)

            var sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST)
            sm.addString(charName!!)
            listOwner.sendPacket(sm)

            val player = World.getPlayer(targetId)

            if (player != null) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST)
                sm.addString(listOwner.name)
                player.sendPacket(sm)
            }
        }

        fun removeFromBlockList(listOwner: Player?, targetId: Int) {
            if (listOwner == null)
                return

            val sm: SystemMessage
            val charName = PlayerInfoTable.getPlayerName(targetId)

            if (!listOwner.blockList.blockList!!.contains(targetId)) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT)
                listOwner.sendPacket(sm)
                return
            }

            listOwner.blockList.removeFromBlockList(targetId)

            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST)
            sm.addString(charName!!)
            listOwner.sendPacket(sm)
        }

        fun isInBlockList(listOwner: Player, target: Player): Boolean {
            return listOwner.blockList.isInBlockList(target)
        }

        fun setBlockAll(listOwner: Player, newValue: Boolean) {
            listOwner.blockList.isBlockAll = newValue
        }

        fun sendListToOwner(listOwner: Player) {
            var i = 1
            listOwner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER)

            for (playerId in listOwner.blockList.blockList!!)
                listOwner.sendMessage(i++.toString() + ". " + PlayerInfoTable.getPlayerName(playerId))

            listOwner.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER)
        }

        /**
         * @param ownerId object id of owner block list
         * @param targetId object id of potential blocked player
         * @return true if blocked
         */
        fun isInBlockList(ownerId: Int, targetId: Int): Boolean {
            val player = World.getPlayer(ownerId)

            if (player != null)
                return BlockList.isBlocked(player, targetId)

            if (!_offlineList.containsKey(ownerId))
                _offlineList[ownerId] = loadList(ownerId)

            return _offlineList[ownerId]?.contains(targetId) ?: false
        }
    }
}