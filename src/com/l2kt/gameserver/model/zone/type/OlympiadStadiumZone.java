package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.data.xml.MapRegionData;
import com.l2kt.gameserver.model.zone.SpawnZoneType;
import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Summon;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.olympiad.OlympiadGameTask;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import com.l2kt.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * A zone extending {@link SpawnZoneType}, used for olympiad event.<br>
 * <br>
 * Restart and the use of "summoning friend" skill aren't allowed. The zone is considered a pvp zone.
 */
public class OlympiadStadiumZone extends SpawnZoneType
{
	OlympiadGameTask _task = null;
	
	public OlympiadStadiumZone(int id)
	{
		super(id);
	}
	
	@Override
	protected final void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		character.setInsideZone(ZoneId.NO_RESTART, true);
		
		if (_task != null && _task.isBattleStarted())
		{
			character.setInsideZone(ZoneId.PVP, true);
			if (character instanceof Player)
			{
				character.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
				_task.getGame().sendOlympiadInfo(character);
			}
		}
		
		// Only participants, observers and GMs are allowed.
		final Player player = character.getActingPlayer();
		if (player != null && !player.isGM() && !player.isInOlympiadMode() && !player.isInObserverMode())
		{
			final Summon summon = player.getPet();
			if (summon != null)
				summon.unSummon(player);
			
			player.teleToLocation(MapRegionData.TeleportType.TOWN);
		}
	}
	
	@Override
	protected final void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		character.setInsideZone(ZoneId.NO_RESTART, false);
		
		if (_task != null && _task.isBattleStarted())
		{
			character.setInsideZone(ZoneId.PVP, false);
			
			if (character instanceof Player)
			{
				character.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				character.sendPacket(ExOlympiadMatchEnd.Companion.getSTATIC_PACKET());
			}
		}
	}
	
	public final void updateZoneStatusForCharactersInside()
	{
		if (_task == null)
			return;
		
		final boolean battleStarted = _task.isBattleStarted();
		final SystemMessage sm = SystemMessage.Companion.getSystemMessage((battleStarted) ? SystemMessageId.ENTERED_COMBAT_ZONE : SystemMessageId.LEFT_COMBAT_ZONE);
		
		for (Creature character : _characters.values())
		{
			if (battleStarted)
			{
				character.setInsideZone(ZoneId.PVP, true);
				if (character instanceof Player)
					character.sendPacket(sm);
			}
			else
			{
				character.setInsideZone(ZoneId.PVP, false);
				if (character instanceof Player)
				{
					character.sendPacket(sm);
					character.sendPacket(ExOlympiadMatchEnd.Companion.getSTATIC_PACKET());
				}
			}
		}
	}
	
	public final void registerTask(OlympiadGameTask task)
	{
		_task = task;
	}
	
	public final void broadcastStatusUpdate(Player player)
	{
		final ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
		for (Player plyr : getKnownTypeInside(Player.class))
		{
			if (plyr.isInObserverMode() || plyr.getOlympiadSide() != player.getOlympiadSide())
				plyr.sendPacket(packet);
		}
	}
	
	public final void broadcastPacketToObservers(L2GameServerPacket packet)
	{
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.isInObserverMode())
				player.sendPacket(packet);
		}
	}
}