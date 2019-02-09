package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.handler.IUserCommandHandler;
import com.l2kt.gameserver.handler.UserCommandHandler;
import com.l2kt.gameserver.model.actor.instance.Player;

public class RequestUserCommand extends L2GameClientPacket
{
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_command = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final IUserCommandHandler handler = UserCommandHandler.getInstance().getHandler(_command);
		if (handler != null)
			handler.useUserCommand(_command, getClient().getActiveChar());
	}
}