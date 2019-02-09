package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.cache.CrestCache;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.serverpackets.ExPledgeCrestLarge;

public final class RequestExPledgeCrestLarge extends L2GameClientPacket
{
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final byte[] data = CrestCache.getInstance().getCrest(CrestCache.CrestType.PLEDGE_LARGE, _crestId);
		if (data == null)
			return;
		
		player.sendPacket(new ExPledgeCrestLarge(_crestId, data));
	}
}