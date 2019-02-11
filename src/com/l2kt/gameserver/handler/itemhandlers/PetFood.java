package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.Config;
import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Pet;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Kerberos
 */
public class PetFood implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		int itemId = item.getItemId();
		switch (itemId)
		{
			case 2515: // Wolf's food
				useFood(playable, 2048, item);
				break;
			case 4038: // Hatchling's food
				useFood(playable, 2063, item);
				break;
			case 5168: // Strider's food
				useFood(playable, 2101, item);
				break;
			case 5169: // ClanHall / Castle Strider's food
				useFood(playable, 2102, item);
				break;
			case 6316: // Wyvern's food
				useFood(playable, 2180, item);
				break;
			case 7582: // Baby Pet's food
				useFood(playable, 2048, item);
				break;
		}
	}
	
	public boolean useFood(Playable activeChar, int magicId, ItemInstance item)
	{
		L2Skill skill = SkillTable.INSTANCE.getInfo(magicId, 1);
		if (skill != null)
		{
			if (activeChar instanceof Pet)
			{
				Pet pet = (Pet) activeChar;
				if (pet.destroyItem("Consume", item.getObjectId(), 1, null, false))
				{
					// Send visual effect.
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0));
					
					// Put current value.
					pet.setCurrentFed(pet.getCurrentFed() + (skill.getFeed() * Config.PET_FOOD_RATE));
					
					// If pet is still hungry, send an alert.
					if (pet.checkAutoFeedState())
						pet.getOwner().sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
					
					return true;
				}
			}
			else if (activeChar instanceof Player)
			{
				final Player player = ((Player) activeChar);
				final int itemId = item.getItemId();
				
				if (player.isMounted() && player.getPetTemplate().canEatFood(itemId))
				{
					if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
					{
						player.broadcastPacket(new MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0));
						player.setCurrentFeed(player.getCurrentFeed() + (skill.getFeed() * Config.PET_FOOD_RATE));
					}
					return true;
				}
				
				activeChar.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
				return false;
			}
		}
		return false;
	}
}