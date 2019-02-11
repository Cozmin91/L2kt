package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.model.pledge.SubPledge

class PledgeReceiveSubPledgeCreated(private val _subPledge: SubPledge, private val _clan: Clan) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x3f)

        writeD(0x01)
        writeD(_subPledge.id)
        writeS(_subPledge.name)
        writeS(_clan.getSubPledgeLeaderName(_subPledge.id))
    }
}