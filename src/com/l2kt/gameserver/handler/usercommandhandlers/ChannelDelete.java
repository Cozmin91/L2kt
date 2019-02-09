package com.l2kt.gameserver.handler.usercommandhandlers;

import com.l2kt.gameserver.handler.IUserCommandHandler;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.group.CommandChannel;
import com.l2kt.gameserver.model.group.Party;

public class ChannelDelete implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		93
	};
	
	@Override
	public boolean useUserCommand(int id, Player player)
	{
		final Party party = player.getParty();
		if (party == null || !party.isLeader(player))
			return false;
		
		final CommandChannel channel = party.getCommandChannel();
		if (channel == null || !channel.isLeader(player))
			return false;
		
		channel.disband();
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}