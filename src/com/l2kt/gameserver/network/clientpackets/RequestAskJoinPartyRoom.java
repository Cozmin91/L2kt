package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
	private static String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		// Send PartyRoom invite request (with activeChar) name to the target
		final Player target = World.getInstance().getPlayer(_name);
		if (target != null)
		{
			if (!target.isProcessingRequest())
			{
				activeChar.onTransactionRequest(target);
				target.sendPacket(new ExAskJoinPartyRoom(activeChar.getName()));
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(target));
		}
		else
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
	}
}