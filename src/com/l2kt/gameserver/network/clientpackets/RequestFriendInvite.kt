package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.FriendAddRequest
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestFriendInvite : L2GameClientPacket() {
    private var _name: String = ""

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val friend = World.getPlayer(_name)

        // can't use friend invite for locating invisible characters
        if (friend == null || !friend.isOnline || friend.appearance.invisible) {
            // Target is not found in the game.
            activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME)
            return
        }

        if (friend == activeChar) {
            // You cannot add yourself to your own friend list.
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST)
            return
        }

        if (BlockList.isBlocked(activeChar, friend)) {
            activeChar.sendMessage("You have blocked $_name.")
            return
        }

        if (BlockList.isBlocked(friend, activeChar)) {
            activeChar.sendMessage("You are in $_name's block list.")
            return
        }

        if (activeChar.friendList.contains(friend.objectId)) {
            // Player already is in your friendlist
            activeChar.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(
                    _name
                )
            )
            return
        }

        if (!friend.isProcessingRequest) {
            // request to become friend
            activeChar.onTransactionRequest(friend)
            friend.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS).addCharName(
                    activeChar
                )
            )
            friend.sendPacket(FriendAddRequest(activeChar.name))
        } else
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_name))
    }
}