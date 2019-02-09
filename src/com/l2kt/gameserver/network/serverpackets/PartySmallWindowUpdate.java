package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.model.actor.instance.Player;

public class PartySmallWindowUpdate extends L2GameServerPacket
{
	private final Player _member;
	
	public PartySmallWindowUpdate(Player member)
	{
		_member = member;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x52);
		writeD(_member.getObjectId());
		writeS(_member.getName());
		
		writeD((int) _member.getCurrentCp()); // c4
		writeD(_member.getMaxCp()); // c4
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		
		writeD(_member.getLevel());
		writeD(_member.getClassId().getId());
	}
}