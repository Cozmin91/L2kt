package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.serverpackets.PackageSendableList;

/**
 * Format: (c)d d: char object id (?)
 * @author -Wooden-
 */
public final class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _objectID;
	
	@Override
	protected void readImpl()
	{
		_objectID = readD();
	}
	
	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final ItemInstance[] items = player.getInventory().getAvailableItems(true, false);
		if (items == null)
			return;
		
		sendPacket(new PackageSendableList(items, _objectID));
	}
}