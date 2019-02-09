package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.serverpackets.SSQStatus;

/**
 * Item Handler for Seven Signs Record
 * @author Tempy
 */
public class SevenSignsRecord implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		playable.sendPacket(new SSQStatus(playable.getObjectId(), 1));
	}
}