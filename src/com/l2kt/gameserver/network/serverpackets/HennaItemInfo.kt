package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.Henna

class HennaItemInfo(private val _henna: Henna, private val _activeChar: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xe3)
        writeD(_henna.symbolId) // symbol Id
        writeD(_henna.dyeId) // item id of dye
        writeD(Henna.requiredDyeAmount) // total amount of dye required
        writeD(_henna.price) // total amount of adenas required to draw symbol
        writeD(1) // able to draw or not 0 is false and 1 is true
        writeD(_activeChar.adena)

        writeD(_activeChar.int) // current INT
        writeC(_activeChar.int + _henna.int) // equip INT
        writeD(_activeChar.str) // current STR
        writeC(_activeChar.str + _henna.str) // equip STR
        writeD(_activeChar.con) // current CON
        writeC(_activeChar.con + _henna.con) // equip CON
        writeD(_activeChar.men) // current MEM
        writeC(_activeChar.men + _henna.men) // equip MEM
        writeD(_activeChar.dex) // current DEX
        writeC(_activeChar.dex + _henna.dex) // equip DEX
        writeD(_activeChar.wit) // current WIT
        writeC(_activeChar.wit + _henna.wit) // equip WIT
    }
}