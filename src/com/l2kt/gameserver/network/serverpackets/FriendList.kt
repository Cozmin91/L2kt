package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import java.util.*

/**
 * Support for "Chat with Friends" dialog.
 * @author Tempy
 */
class FriendList(player: Player) : L2GameServerPacket() {
    private val _info: MutableList<FriendInfo>

    private class FriendInfo(internal var _objId: Int, internal var _name: String, internal var _online: Boolean)

    init {
        _info = ArrayList(player.friendList.size)

        for (objId in player.friendList) {
            val name = PlayerInfoTable.getPlayerName(objId) ?: ""
            val player1 = World.getPlayer(objId)

            _info.add(FriendInfo(objId, name, player1 != null && player1.isOnline))
        }
    }

    override fun writeImpl() {
        writeC(0xfa)
        writeD(_info.size)
        for (info in _info) {
            writeD(info._objId)
            writeS(info._name)
            writeD(if (info._online) 0x01 else 0x00)
            writeD(if (info._online) info._objId else 0x00)
        }
    }
}