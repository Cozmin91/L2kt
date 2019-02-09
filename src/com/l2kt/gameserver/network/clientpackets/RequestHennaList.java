package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.xml.HennaData;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.serverpackets.HennaEquipList;

public final class RequestHennaList extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readD(); // ??
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new HennaEquipList(activeChar, HennaData.getInstance().getAvailableHennasFor(activeChar)));
	}
}