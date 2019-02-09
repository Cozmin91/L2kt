package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.partymatching.PartyMatchRoom;
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList;
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2kt.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2kt.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public final class RequestPartyMatchDetail extends L2GameClientPacket
{
	private int _roomid;
	@SuppressWarnings("unused")
	private int _unk1;
	@SuppressWarnings("unused")
	private int _unk2;
	@SuppressWarnings("unused")
	private int _unk3;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		/*
		 * IF player click on Room all unk are 0 IF player click AutoJoin values are -1 1 1
		 */
		_unk1 = readD();
		_unk2 = readD();
		_unk3 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		if (room == null)
			return;
		
		if ((activeChar.getLevel() >= room.getMinLvl()) && (activeChar.getLevel() <= room.getMaxLvl()))
		{
			// Remove from waiting list
			PartyMatchWaitingList.getInstance().removePlayer(activeChar);
			
			activeChar.setPartyRoom(_roomid);
			
			activeChar.sendPacket(new PartyMatchDetail(room));
			activeChar.sendPacket(new ExPartyRoomMember(room, 0));
			
			for (Player member : room.getPartyMembers())
			{
				if (member == null)
					continue;
				
				member.sendPacket(new ExManagePartyRoomMember(activeChar, room, 0));
				member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM).addCharName(activeChar));
			}
			room.addMember(activeChar);
			
			// Info Broadcast
			activeChar.broadcastUserInfo();
		}
		else
			activeChar.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM);
	}
}