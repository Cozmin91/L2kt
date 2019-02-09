package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.sql.ClanTable;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;

public final class RequestReplyStopPledgeWar extends L2GameClientPacket
{
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final Player requestor = activeChar.getActiveRequester();
		if (requestor == null)
			return;
		
		if (_answer == 1)
			ClanTable.getInstance().deleteClansWars(requestor.getClanId(), activeChar.getClanId());
		else
			requestor.sendPacket(SystemMessageId.REQUEST_TO_END_WAR_HAS_BEEN_DENIED);
		
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}