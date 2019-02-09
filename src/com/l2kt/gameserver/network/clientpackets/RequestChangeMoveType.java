package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;

public final class RequestChangeMoveType extends L2GameClientPacket
{
	private boolean _typeRun;
	
	@Override
	protected void readImpl()
	{
		_typeRun = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		// Get player.
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		// Player is mounted, do not allow to change movement type.
		if (player.isMounted())
			return;
		
		// Change movement type.
		if (_typeRun)
			player.setRunning();
		else
			player.setWalking();
	}
}