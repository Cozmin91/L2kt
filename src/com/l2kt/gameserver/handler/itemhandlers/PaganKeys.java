package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.gameserver.data.xml.DoorData;
import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Door;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;

public class PaganKeys implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		final WorldObject target = activeChar.getTarget();
		
		if (!(target instanceof Door))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			return;
		}
		
		final Door door = (Door) target;
		
		if (!(activeChar.isInsideRadius(door, Npc.INTERACTION_DISTANCE, false, false)))
		{
			activeChar.sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
			activeChar.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			return;
		}
		
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, true))
			return;
		
		final int doorId = door.getDoorId();
		
		switch (item.getItemId())
		{
			case 8056:
				if (doorId == 23150004 || doorId == 23150003)
				{
					DoorData.INSTANCE.getDoor(23150003).openMe();
					DoorData.INSTANCE.getDoor(23150004).openMe();
				}
				else
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				break;
			
			case 8273:
				switch (doorId)
				{
					case 19160002:
					case 19160003:
					case 19160004:
					case 19160005:
					case 19160006:
					case 19160007:
					case 19160008:
					case 19160009:
						DoorData.INSTANCE.getDoor(doorId).openMe();
						break;
					
					default:
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						break;
				}
				break;
			
			case 8275:
				switch (doorId)
				{
					case 19160012:
					case 19160013:
						DoorData.INSTANCE.getDoor(doorId).openMe();
						break;
					
					default:
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						break;
				}
				break;
		}
	}
}