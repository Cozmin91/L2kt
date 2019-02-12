package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.L2ShortCut
import com.l2kt.gameserver.network.serverpackets.ShortCutRegister

class RequestShortCutReg : L2GameClientPacket() {
    private var _type: Int = 0
    private var _id: Int = 0
    private var _slot: Int = 0
    private var _page: Int = 0
    private var _characterType: Int = 0

    override fun readImpl() {
        _type = readD()
        val slot = readD()
        _id = readD()
        _characterType = readD()

        _slot = slot % 12
        _page = slot / 12
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (_page > 10 || _page < 0)
            return

        when (_type) {
            0x01 // item
                , 0x03 // action
                , 0x04 // macro
                , 0x05 // recipe
            -> {
                val sc = L2ShortCut(_slot, _page, _type, _id, -1, _characterType)
                sendPacket(ShortCutRegister(sc))
                activeChar.registerShortCut(sc)
            }
            0x02 // skill
            -> {
                val level = activeChar.getSkillLevel(_id)
                if (level > 0) {
                    val sc = L2ShortCut(_slot, _page, _type, _id, level, _characterType)
                    sendPacket(ShortCutRegister(sc))
                    activeChar.registerShortCut(sc)
                }
            }
        }
    }
}