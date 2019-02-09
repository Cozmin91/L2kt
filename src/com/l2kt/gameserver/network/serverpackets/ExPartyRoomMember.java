package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.data.xml.MapRegionData;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.partymatching.PartyMatchRoom;

public class ExPartyRoomMember extends L2GameServerPacket
{
	private final PartyMatchRoom _room;
	private final int _mode;
	
	public ExPartyRoomMember(PartyMatchRoom room, int mode)
	{
		_room = room;
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x0e);
		writeD(_mode);
		writeD(_room.getMembers());
		for (Player member : _room.getPartyMembers())
		{
			writeD(member.getObjectId());
			writeS(member.getName());
			writeD(member.getActiveClass());
			writeD(member.getLevel());
			writeD(MapRegionData.getInstance().getClosestLocation(member.getX(), member.getY()));
			if (_room.getOwner().equals(member))
				writeD(1);
			else
			{
				if ((_room.getOwner().isInParty() && member.isInParty()) && (_room.getOwner().getParty().getLeaderObjectId() == member.getParty().getLeaderObjectId()))
					writeD(2);
				else
					writeD(0);
			}
		}
	}
}