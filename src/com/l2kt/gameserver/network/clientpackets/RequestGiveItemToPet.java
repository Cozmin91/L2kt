package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.commons.math.MathUtil;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Pet;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.type.EtcItemType;
import com.l2kt.gameserver.network.SystemMessageId;

import com.l2kt.gameserver.network.serverpackets.EnchantResult;

public final class RequestGiveItemToPet extends L2GameClientPacket
{
	private int _objectId;
	private int _amount;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (_amount <= 0)
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null || !player.hasPet())
			return;
		
		// Alt game - Karma punishment
		if (!Config.KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0)
		{
			player.sendMessage("You cannot trade in a chaotic state.");
			return;
		}
		
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		final ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		if (item == null || item.isAugmented())
			return;
		
		if (item.isHeroItem() || !item.isDropable() || !item.isDestroyable() || !item.isTradable() || item.getItem().getItemType() == EtcItemType.ARROW || item.getItem().getItemType() == EtcItemType.SHOT)
		{
			player.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}
		
		final Pet pet = (Pet) player.getPet();
		if (pet.isDead())
		{
			player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
			return;
		}
		
		if (MathUtil.INSTANCE.calculateDistance(player, pet, true) > Npc.INTERACTION_DISTANCE)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}
		
		if (!pet.getInventory().validateCapacity(item))
		{
			player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
			return;
		}
		
		if (!pet.getInventory().validateWeight(item, _amount))
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			player.setActiveEnchantItem(null);
			player.sendPacket(EnchantResult.Companion.getCANCELLED());
			player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		}
		
		player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet);
	}
}