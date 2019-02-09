package com.l2kt.gameserver.model.zone.type;

import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.zone.ZoneType;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * A zone extending {@link ZoneType}, used for hp/mp regen boost. Notably used by Mother Tree. It has a Race condition, and allow a entrance and exit message.
 */
public class MotherTreeZone extends ZoneType
{
	private int _enterMsg;
	private int _leaveMsg;
	
	private int _mpRegen = 1;
	private int _hpRegen = 1;
	private int _race = -1;
	
	public MotherTreeZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("enterMsgId"))
			_enterMsg = Integer.valueOf(value);
		else if (name.equals("leaveMsgId"))
			_leaveMsg = Integer.valueOf(value);
		else if (name.equals("MpRegenBonus"))
			_mpRegen = Integer.valueOf(value);
		else if (name.equals("HpRegenBonus"))
			_hpRegen = Integer.valueOf(value);
		else if (name.equals("affectedRace"))
			_race = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected boolean isAffected(Creature character)
	{
		if (character instanceof Player)
			return _race == ((Player) character).getRace().ordinal();
		
		return true;
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.MOTHER_TREE, true);
			
			if (_enterMsg != 0)
				character.sendPacket(SystemMessage.getSystemMessage(_enterMsg));
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.MOTHER_TREE, false);
			
			if (_leaveMsg != 0)
				character.sendPacket(SystemMessage.getSystemMessage(_leaveMsg));
		}
	}
	
	public int getMpRegenBonus()
	{
		return _mpRegen;
	}
	
	public int getHpRegenBonus()
	{
		return _hpRegen;
	}
}