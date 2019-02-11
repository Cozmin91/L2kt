package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public class Elixir extends ItemSkills
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
		{
			playable.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
			return;
		}
		super.useItem(playable, item, forceUse);
	}
}