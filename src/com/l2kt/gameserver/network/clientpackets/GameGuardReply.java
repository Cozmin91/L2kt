package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;

/**
 * @author zabbix
 */
public class GameGuardReply extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		getClient().setGameGuardOk(true);
	}
}