package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.xml.MapRegionData;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.partymatching.PartyMatchRoom;
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList;
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2kt.gameserver.network.serverpackets.PartyMatchList;

public final class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private int _charid;
	
	@Override
	protected void readImpl()
	{
		_charid = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final Player member = World.getInstance().getPlayer(_charid);
		if (member == null)
			return;
		
		final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(member);
		if (room == null)
			return;
		
		if (room.getOwner() != activeChar)
			return;
		
		if (activeChar.isInParty() && member.isInParty() && activeChar.getParty().getLeaderObjectId() == member.getParty().getLeaderObjectId())
			activeChar.sendPacket(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER);
		else
		{
			room.deleteMember(member);
			member.setPartyRoom(0);
			
			// Close the PartyRoom window
			member.sendPacket(ExClosePartyRoom.STATIC_PACKET);
			
			// Add player back on waiting list
			PartyMatchWaitingList.getInstance().addPlayer(member);
			
			// Send Room list
			member.sendPacket(new PartyMatchList(member, 0, MapRegionData.getInstance().getClosestLocation(member.getX(), member.getY()), member.getLevel()));
			
			// Clean player's LFP title
			member.broadcastUserInfo();
			
			member.sendPacket(SystemMessageId.OUSTED_FROM_PARTY_ROOM);
		}
	}
}