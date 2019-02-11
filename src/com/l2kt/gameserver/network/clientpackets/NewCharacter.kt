package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.PlayerData
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.CharTemplates

class NewCharacter : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val ct = CharTemplates()

        ct.addChar(PlayerData.getInstance().getTemplate(0))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.HUMAN_FIGHTER))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.HUMAN_MYSTIC))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.ELVEN_FIGHTER))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.ELVEN_MYSTIC))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.DARK_FIGHTER))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.DARK_MYSTIC))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.ORC_FIGHTER))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.ORC_MYSTIC))
        ct.addChar(PlayerData.getInstance().getTemplate(ClassId.DWARVEN_FIGHTER))

        sendPacket(ct)
    }
}