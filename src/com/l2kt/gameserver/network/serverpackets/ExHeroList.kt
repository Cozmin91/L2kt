package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.model.olympiad.Olympiad
import com.l2kt.gameserver.templates.StatsSet

/**
 * Format: (ch) d [SdSdSdd]
 * @author -Wooden-, KenM, godson
 */
class ExHeroList : L2GameServerPacket() {
    private val _heroList: Collection<StatsSet>

    init {
        _heroList = Hero.getInstance().heroes.values
    }

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x23)
        writeD(_heroList.size)

        for (hero in _heroList) {
            writeS(hero.getString(Olympiad.CHAR_NAME))
            writeD(hero.getInteger(Olympiad.CLASS_ID))
            writeS(hero.getString(Hero.CLAN_NAME, ""))
            writeD(hero.getInteger(Hero.CLAN_CREST, 0))
            writeS(hero.getString(Hero.ALLY_NAME, ""))
            writeD(hero.getInteger(Hero.ALLY_CREST, 0))
            writeD(hero.getInteger(Hero.COUNT))
        }
    }
}