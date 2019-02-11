package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.L2ShortCut

/**
 * format dd d/dd/d d
 */
class ShortCutRegister(private val _shortcut: L2ShortCut) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x44)

        writeD(_shortcut.type)
        writeD(_shortcut.slot + _shortcut.page * 12) // C4 Client
        when (_shortcut.type) {
            L2ShortCut.TYPE_ITEM // 1
            -> {
                writeD(_shortcut.id)
                writeD(_shortcut.characterType)
                writeD(_shortcut.sharedReuseGroup)
            }
            L2ShortCut.TYPE_SKILL // 2
            -> {
                writeD(_shortcut.id)
                writeD(_shortcut.level)
                writeC(0x00) // C5
                writeD(_shortcut.characterType)
            }
            else -> {
                writeD(_shortcut.id)
                writeD(_shortcut.characterType)
            }
        }
    }
}