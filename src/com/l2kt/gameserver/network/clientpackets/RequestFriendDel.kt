package com.l2kt.gameserver.network.clientpackets

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.FriendList
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestFriendDel : L2GameClientPacket() {

    private var _name: String = ""

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val friendId = PlayerInfoTable.getPlayerObjectId(_name)
        if (friendId == -1 || !player.friendList.contains(friendId)) {
            player.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST).addString(
                    _name
                )
            )
            return
        }

        // Player deleted from your friendlist
        player.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(
                _name
            )
        )

        player.friendList.remove(Integer.valueOf(friendId))
        player.sendPacket(FriendList(player)) // update friendList *heavy method*

        val friend = World.getPlayer(_name)
        if (friend != null) {
            friend.friendList.remove(Integer.valueOf(player.objectId))
            friend.sendPacket(FriendList(friend)) // update friendList *heavy method*
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_FRIEND).use { ps ->
                    ps.setInt(1, player.objectId)
                    ps.setInt(2, friendId)
                    ps.setInt(3, friendId)
                    ps.setInt(4, player.objectId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            L2GameClientPacket.LOGGER.error(
                "Couldn't delete friendId {} for {}.",
                e,
                friendId,
                player.toString()
            )
        }

    }

    companion object {
        private const val DELETE_FRIEND =
            "DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)"
    }
}