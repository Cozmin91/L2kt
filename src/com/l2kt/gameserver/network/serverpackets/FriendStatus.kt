package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.World

/**
 * Support for "Chat with Friends" dialog. <BR></BR>
 * Inform player about friend online status change <BR></BR>
 * Format: cdSd<BR></BR>
 * d: Online/Offline<BR></BR>
 * S: Friend Name <BR></BR>
 * d: Player Object ID <BR></BR>
 * @author JIV
 */
class FriendStatus(private val _objid: Int) : L2GameServerPacket() {
    private val _online: Boolean = World.getPlayer(_objid) != null
    private val _name: String = PlayerInfoTable.getPlayerName(_objid) ?: ""

    override fun writeImpl() {
        writeC(0x7b)
        writeD(if (_online) 1 else 0)
        writeS(_name)
        writeD(_objid)
    }
}