package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.xml.HennaData;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.Henna;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.SystemMessageId;

public final class RequestHennaEquip extends L2GameClientPacket
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
		
		final Henna henna = HennaData.getInstance().getHenna(_symbolId);
		if (henna == null)
			return;
		
		if (!henna.canBeUsedBy(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			return;
		}
		
		if (activeChar.getHennaEmptySlots() == 0)
		{
			activeChar.sendPacket(SystemMessageId.SYMBOLS_FULL);
			return;
		}
		
		final ItemInstance ownedDyes = activeChar.getInventory().getItemByItemId(henna.getDyeId());
		final int count = (ownedDyes == null) ? 0 : ownedDyes.getCount();
		
		if (count < Henna.getRequiredDyeAmount())
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			return;
		}
		
		// reduceAdena sends a message.
		if (!activeChar.reduceAdena("Henna", henna.getPrice(), activeChar.getCurrentFolk(), true))
			return;
		
		// destroyItemByItemId sends a message.
		if (!activeChar.destroyItemByItemId("Henna", henna.getDyeId(), Henna.getRequiredDyeAmount(), activeChar, true))
			return;
		
		activeChar.addHenna(henna);
	}
}