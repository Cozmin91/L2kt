package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.group.Party;
import com.l2kt.gameserver.model.partymatching.PartyMatchRoom;
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList;
import com.l2kt.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2kt.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2kt.gameserver.network.serverpackets.PartyMatchDetail;

public final class RequestWithdrawParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final Party party = player.getParty();
		if (party == null)
			return;
		
		party.removePartyMember(player, Party.MessageType.LEFT);
		
		if (player.isInPartyMatchRoom())
		{
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
			if (room != null)
			{
				player.sendPacket(new PartyMatchDetail(room));
				player.sendPacket(new ExPartyRoomMember(room, 0));
				player.sendPacket(ExClosePartyRoom.STATIC_PACKET);
				
				room.deleteMember(player);
			}
			player.setPartyRoom(0);
			player.broadcastUserInfo();
		}
	}
}