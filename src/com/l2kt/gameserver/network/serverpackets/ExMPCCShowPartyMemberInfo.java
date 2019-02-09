package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.group.Party;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private final Party _party;
	
	public ExMPCCShowPartyMemberInfo(Party party)
	{
		_party = party;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4a);
		
		writeD(_party.getMembersCount());
		for (Player member : _party.getMembers())
		{
			writeS(member.getName());
			writeD(member.getObjectId());
			writeD(member.getClassId().getId());
		}
	}
}