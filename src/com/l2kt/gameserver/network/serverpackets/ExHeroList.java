package com.l2kt.gameserver.network.serverpackets;

import java.util.Collection;

import com.l2kt.gameserver.model.entity.Hero;
import com.l2kt.gameserver.model.olympiad.Olympiad;
import com.l2kt.gameserver.templates.StatsSet;

/**
 * Format: (ch) d [SdSdSdd]
 * @author -Wooden-, KenM, godson
 */
public class ExHeroList extends L2GameServerPacket
{
	private final Collection<StatsSet> _heroList;
	
	public ExHeroList()
	{
		_heroList = Hero.getInstance().getHeroes().values();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x23);
		writeD(_heroList.size());
		
		for (StatsSet hero : _heroList)
		{
			writeS(hero.getString(Olympiad.CHAR_NAME));
			writeD(hero.getInteger(Olympiad.CLASS_ID));
			writeS(hero.getString(Hero.CLAN_NAME, ""));
			writeD(hero.getInteger(Hero.CLAN_CREST, 0));
			writeS(hero.getString(Hero.ALLY_NAME, ""));
			writeD(hero.getInteger(Hero.ALLY_CREST, 0));
			writeD(hero.getInteger(Hero.COUNT));
		}
	}
}