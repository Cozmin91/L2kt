package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.network.serverpackets.SSQStatus

/**
 * Seven Signs Record Update Request packet type id 0xc7 format: cc
 * @author Tempy
 */
class RequestSSQStatus : L2GameClientPacket() {
    private var _page: Int = 0

    override fun readImpl() {
        _page = readC()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if ((SevenSigns.getInstance().isSealValidationPeriod || SevenSigns.getInstance().isCompResultsPeriod) && _page == 4)
            return

        activeChar.sendPacket(SSQStatus(activeChar.objectId, _page))
    }
}