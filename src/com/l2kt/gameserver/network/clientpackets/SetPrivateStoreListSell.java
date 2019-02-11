package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.tradelist.TradeList;
import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.PrivateStoreManageListSell;
import com.l2kt.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager;

public final class SetPrivateStoreListSell extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item
	
	private boolean _packageSale;
	private Item[] _items = null;
	
	@Override
	protected void readImpl()
	{
		_packageSale = (readD() == 1);
		int count = readD();
		if (count < 1 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
			return;
		
		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readD();
			int price = readD();
			
			if (itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(itemId, (int) cnt, price);
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (_items == null)
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			player.setStoreType(Player.StoreType.NONE);
			player.broadcastUserInfo();
			player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (AttackStanceTaskManager.INSTANCE.isInAttackStance(player) || (player.isCastingNow() || player.isCastingSimultaneouslyNow()) || player.isInDuel())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_STORE))
		{
			player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (_items.length > player.getPrivateSellStoreLimit())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			return;
		}
		
		TradeList tradeList = player.getSellList();
		tradeList.clear();
		tradeList.setPackaged(_packageSale);
		
		int totalCost = player.getAdena();
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
				return;
			}
			
			totalCost += i.getPrice();
			if (totalCost > Integer.MAX_VALUE)
			{
				player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
				return;
			}
		}
		
		player.sitDown();
		player.setStoreType((_packageSale) ? Player.StoreType.PACKAGE_SELL : Player.StoreType.SELL);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgSell(player));
	}
	
	private static class Item
	{
		private final int _itemId, _count, _price;
		
		public Item(int id, int num, int pri)
		{
			_itemId = id;
			_count = num;
			_price = pri;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if ((Integer.MAX_VALUE / _count) < _price)
				return false;
			
			list.addItem(_itemId, _count, _price);
			return true;
		}
		
		public long getPrice()
		{
			return _count * _price;
		}
	}
}