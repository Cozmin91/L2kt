package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.gameserver.model.actor.instance.Folk;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.holder.IntIntHolder;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.itemcontainer.ItemContainer;
import com.l2kt.gameserver.model.itemcontainer.PcInventory;
import com.l2kt.gameserver.model.itemcontainer.PcWarehouse;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.EnchantResult;
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate;
import com.l2kt.gameserver.network.serverpackets.StatusUpdate;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public final class SendWarehouseDepositList extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 8; // length of one item
	
	private IntIntHolder _items[] = null;
	
	@Override
	protected void readImpl()
	{
		final int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
			return;
		
		_items = new IntIntHolder[count];
		for (int i = 0; i < count; i++)
		{
			int objId = readD();
			int cnt = readD();
			
			if (objId < 1 || cnt < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new IntIntHolder(objId, cnt);
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items == null)
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			player.setActiveEnchantItem(null);
			player.sendPacket(EnchantResult.Companion.getCANCELLED());
			player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		}
		
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
			return;
		
		final boolean isPrivate = warehouse instanceof PcWarehouse;
		
		final Folk folk = player.getCurrentFolk();
		if (folk == null || !folk.isWarehouse() || !folk.canInteract(player))
			return;
		
		if (!isPrivate && !player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0)
			return;
		
		// Freight price from config or normal price per item slot (30)
		final int fee = _items.length * 30;
		int currentAdena = player.getAdena();
		int slots = 0;
		
		for (IntIntHolder i : _items)
		{
			ItemInstance item = player.checkItemManipulation(i.getId(), i.getValue());
			if (item == null)
				return;
			
			// Calculate needed adena and slots
			if (item.getItemId() == PcInventory.ADENA_ID)
				currentAdena -= i.getValue();
			
			if (!item.isStackable())
				slots += i.getValue();
			else if (warehouse.getItemByItemId(item.getItemId()) == null)
				slots++;
		}
		
		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		
		// Check if enough adena and charge the fee
		if (currentAdena < fee || !player.reduceAdena(warehouse.getName(), fee, folk, false))
		{
			sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		// get current tradelist if any
		if (player.getActiveTradeList() != null)
			return;
		
		// Proceed to the transfer
		InventoryUpdate playerIU = new InventoryUpdate();
		for (IntIntHolder i : _items)
		{
			// Check validity of requested item
			ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getValue());
			if (oldItem == null)
				return;
			
			if (!oldItem.isDepositable(isPrivate) || !oldItem.isAvailable(player, true, isPrivate))
				continue;
			
			final ItemInstance newItem = player.getInventory().transferItem(warehouse.getName(), i.getId(), i.getValue(), warehouse, player, folk);
			if (newItem == null)
				continue;
			
			if (oldItem.getCount() > 0 && oldItem != newItem)
				playerIU.addModifiedItem(oldItem);
			else
				playerIU.addRemovedItem(oldItem);
		}
		
		// Send updated item list to the player
		player.sendPacket(playerIU);
		
		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}