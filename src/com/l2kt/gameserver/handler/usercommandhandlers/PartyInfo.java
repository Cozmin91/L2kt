package com.l2kt.gameserver.handler.usercommandhandlers;

import com.l2kt.gameserver.handler.IUserCommandHandler;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.group.Party;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public class PartyInfo implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		81
	};
	
	@Override
	public boolean useUserCommand(int id, Player player)
	{
		final Party party = player.getParty();
		if (party == null)
			return false;
		
		player.sendPacket(SystemMessageId.PARTY_INFORMATION);
		player.sendPacket(party.getLootRule().getMessageId());
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_LEADER_S1).addString(party.getLeader().getName()));
		player.sendMessage("Members: " + party.getMembersCount() + "/9");
		player.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}