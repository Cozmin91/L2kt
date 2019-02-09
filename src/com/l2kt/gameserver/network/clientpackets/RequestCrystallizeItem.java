package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.type.CrystalType;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

import static com.l2kt.gameserver.model.item.type.CrystalType.*;

public final class RequestCrystallizeItem extends L2GameClientPacket
{
	private int _objectId;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// Sanity check.
		if (_count <= 0)
			return;
		
		// Client must be attached to a player instance.
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		// Player mustn't be already crystallizing or in store mode.
		if (player.isInStoreMode() || player.isCrystallizing())
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		// Player must own Crystallize skill.
		final int skillLevel = player.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0)
		{
			player.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			return;
		}
		
		// Item must exist on player inventory. It must be a crystallizable item.
		final ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		if (item == null || item.isHeroItem() || item.isShadowItem())
			return;
		
		if (!item.getItem().isCrystallizable() || item.getItem().getCrystalCount() <= 0 || item.getItem().getCrystalType() == NONE)
			return;
		
		// Sanity check for count.
		_count = Math.min(_count, item.getCount());
		
		// Check if the player can crystallize items and return if false.
		boolean canCrystallize = true;
		
		switch (item.getItem().getCrystalType())
		{
			case C:
				if (skillLevel <= 1)
					canCrystallize = false;
				break;
			
			case B:
				if (skillLevel <= 2)
					canCrystallize = false;
				break;
			
			case A:
				if (skillLevel <= 3)
					canCrystallize = false;
				break;
			
			case S:
				if (skillLevel <= 4)
					canCrystallize = false;
				break;
		}
		
		if (!canCrystallize)
		{
			player.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.setCrystallizing(true);
		
		// Unequip the item if needed.
		if (item.isEquipped())
		{
			InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance items : player.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot()))
				iu.addModifiedItem(items);
			
			player.sendPacket(iu);
			
			SystemMessage msg;
			if (item.getEnchantLevel() > 0)
				msg = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			else
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item.getItemId());
			
			player.sendPacket(msg);
		}
		
		// Remove the item from inventory.
		ItemInstance removedItem = player.getInventory().destroyItem("Crystalize", _objectId, _count, player, null);
		
		InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(removedItem);
		player.sendPacket(iu);
		
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED).addItemName(removedItem.getItemId()));
		
		// add crystals
		ItemInstance crystals = player.getInventory().addItem("Crystalize", item.getItem().getCrystalItemId(), item.getCrystalCount(), player, player);
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystals.getItemId()).addItemNumber(item.getCrystalCount()));
		
		player.broadcastUserInfo();
		player.setCrystallizing(false);
	}
}