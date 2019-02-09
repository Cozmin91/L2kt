package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.group.CommandChannel;
import com.l2kt.gameserver.model.group.Party;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public final class RequestExOustFromMPCC extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player requestor = getClient().getActiveChar();
		if (requestor == null)
			return;
		
		final Player target = World.getInstance().getPlayer(_name);
		if (target == null)
		{
			requestor.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return;
		}
		
		if (requestor.equals(target))
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final Party requestorParty = requestor.getParty();
		final Party targetParty = target.getParty();
		
		if (requestorParty == null || targetParty == null)
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final CommandChannel requestorChannel = requestorParty.getCommandChannel();
		if (requestorChannel == null || !requestorChannel.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (!requestorChannel.removeParty(targetParty))
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		targetParty.broadcastMessage(SystemMessageId.DISMISSED_FROM_COMMAND_CHANNEL);
		
		// check if CC has not been canceled
		if (requestorParty.isInCommandChannel())
			requestorParty.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL).addCharName(targetParty.getLeader()));
	}
}