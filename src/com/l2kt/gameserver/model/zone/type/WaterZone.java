package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.zone.ZoneType;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import com.l2kt.gameserver.network.serverpackets.ServerObjectInfo;

/**
 * A zone extending {@link ZoneType}, used for the water behavior. {@link Player}s can drown if they stay too long below water line.
 */
public class WaterZone extends ZoneType
{
	public WaterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.WATER, true);
		
		if (character instanceof Player)
			((Player) character).broadcastUserInfo();
		else if (character instanceof Npc)
		{
			for (Player player : character.getKnownType(Player.class))
			{
				if (character.getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo((Npc) character, player));
				else
					player.sendPacket(new NpcInfo((Npc) character, player));
			}
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.WATER, false);
		
		if (character instanceof Player)
			((Player) character).broadcastUserInfo();
		else if (character instanceof Npc)
		{
			for (Player player : character.getKnownType(Player.class))
			{
				if (character.getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo((Npc) character, player));
				else
					player.sendPacket(new NpcInfo((Npc) character, player));
			}
		}
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}