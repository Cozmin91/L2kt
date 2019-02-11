package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.gameserver.model.actor.instance.Folk;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.holder.IntIntHolder;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.itemcontainer.ClanWarehouse;
import com.l2kt.gameserver.model.itemcontainer.ItemContainer;
import com.l2kt.gameserver.model.itemcontainer.PcWarehouse;
import com.l2kt.gameserver.model.pledge.Clan;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.EnchantResult;
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate;
import com.l2kt.gameserver.network.serverpackets.StatusUpdate;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public final class SendWarehouseWithdrawList extends L2GameClientPacket
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
		
		final Folk folk = player.getCurrentFolk();
		if (folk == null || !folk.isWarehouse() || !folk.canInteract(player))
			return;
		
		if (!(warehouse instanceof PcWarehouse) && !player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0)
			return;
		
		if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
		{
			if (warehouse instanceof ClanWarehouse && ((player.getClanPrivileges() & Clan.CP_CL_VIEW_WAREHOUSE) != Clan.CP_CL_VIEW_WAREHOUSE))
				return;
		}
		else
		{
			if (warehouse instanceof ClanWarehouse && !player.isClanLeader())
			{
				// this msg is for depositing but maybe good to send some msg?
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
				return;
			}
		}
		
		int weight = 0;
		int slots = 0;
		
		for (IntIntHolder i : _items)
		{
			// Calculate needed slots
			ItemInstance item = warehouse.getItemByObjectId(i.getId());
			if (item == null || item.getCount() < i.getValue())
				return;
			
			weight += i.getValue() * item.getItem().getWeight();
			
			if (!item.isStackable())
				slots += i.getValue();
			else if (player.getInventory().getItemByItemId(item.getItemId()) == null)
				slots++;
		}
		
		// Item Max Limit Check
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		
		// Weight limit Check
		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		// Proceed to the transfer
		InventoryUpdate playerIU = new InventoryUpdate();
		for (IntIntHolder i : _items)
		{
			ItemInstance oldItem = warehouse.getItemByObjectId(i.getId());
			if (oldItem == null || oldItem.getCount() < i.getValue())
				return;
			
			final ItemInstance newItem = warehouse.transferItem(warehouse.getName(), i.getId(), i.getValue(), player.getInventory(), player, folk);
			if (newItem == null)
				return;
			
			if (newItem.getCount() > i.getValue())
				playerIU.addModifiedItem(newItem);
			else
				playerIU.addNewItem(newItem);
		}
		
		// Send updated item list to the player
		player.sendPacket(playerIU);
		
		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}