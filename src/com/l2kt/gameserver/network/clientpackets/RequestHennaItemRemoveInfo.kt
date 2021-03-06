package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.HennaData
import com.l2kt.gameserver.network.serverpackets.HennaItemRemoveInfo

class RequestHennaItemRemoveInfo : L2GameClientPacket() {
    private var _symbolId: Int = 0

    override fun readImpl() {
        _symbolId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val template = HennaData.getHenna(_symbolId) ?: return

        activeChar.sendPacket(HennaItemRemoveInfo(template, activeChar))
    }
}