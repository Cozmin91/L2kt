package com.l2kt.gameserver.network.clientpackets

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.FriendList
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestAnswerFriendInvite : L2GameClientPacket() {

    private var _response: Int = 0

    override fun readImpl() {
        _response = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val requestor = player.activeRequester ?: return

        if (_response == 1) {
            requestor.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND)

            // Player added to your friendlist
            requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS).addCharName(player))
            requestor.friendList.add(player.objectId)

            // has joined as friend.
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND).addCharName(requestor))
            player.friendList.add(requestor.objectId)

            // update friendLists *heavy method*
            requestor.sendPacket(FriendList(requestor))
            player.sendPacket(FriendList(player))

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(ADD_FRIEND).use { ps ->
                        ps.setInt(1, requestor.objectId)
                        ps.setInt(2, player.objectId)
                        ps.setInt(3, player.objectId)
                        ps.setInt(4, requestor.objectId)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                L2GameClientPacket.Companion.LOGGER.error(
                    "Couldn't add friendId {} for {}.",
                    e,
                    player.objectId,
                    requestor.toString()
                )
            }

        } else
            requestor.sendPacket(SystemMessageId.FAILED_TO_INVITE_A_FRIEND)

        player.activeRequester = null
        requestor.onTransactionResponse()
    }

    companion object {
        private const val ADD_FRIEND = "INSERT INTO character_friends (char_id, friend_id) VALUES (?,?), (?,?)"
    }
}