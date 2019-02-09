package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.xml.HennaData;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.Henna;
import com.l2kt.gameserver.network.serverpackets.HennaItemInfo;

public final class RequestHennaItemInfo extends L2GameClientPacket
{
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final Henna template = HennaData.getInstance().getHenna(_symbolId);
		if (template == null)
			return;
		
		activeChar.sendPacket(new HennaItemInfo(template, activeChar));
	}
}