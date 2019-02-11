package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestFriendList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER)

        for (id in activeChar.friendList) {
            val friendName = PlayerInfoTable.getInstance().getPlayerName(id) ?: continue

            val friend = World.getInstance().getPlayer(id)

            activeChar.sendPacket(
                SystemMessage.getSystemMessage(if (friend == null || !friend.isOnline) SystemMessageId.S1_OFFLINE else SystemMessageId.S1_ONLINE).addString(
                    friendName
                )
            )
        }
        activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER)
    }
}