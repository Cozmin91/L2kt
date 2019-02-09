package com.l2kt.gameserver.network.serverpackets;

import java.util.Set;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.kind.Item;
import com.l2kt.gameserver.model.item.kind.Weapon;
import com.l2kt.gameserver.model.pledge.Clan;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private final Set<ItemInstance> _items;
	private final String _playerName;
	private final int _money;
	
	public GMViewWarehouseWithdrawList(Player player)
	{
		_items = player.getWarehouse().getItems();
		_playerName = player.getName();
		_money = player.getWarehouse().getAdena();
	}
	
	public GMViewWarehouseWithdrawList(Clan clan)
	{
		_playerName = clan.getLeaderName();
		_items = clan.getWarehouse().getItems();
		_money = clan.getWarehouse().getAdena();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x95);
		writeS(_playerName);
		writeD(_money);
		writeH(_items.size());
		
		for (ItemInstance temp : _items)
		{
			Item item = temp.getItem();
			
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeD(item.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.isWeapon() ? ((Weapon) item).getSoulShotCount() : 0x00);
			writeH(temp.isWeapon() ? ((Weapon) item).getSpiritShotCount() : 0x00);
			writeD(temp.getObjectId());
			writeD((temp.isWeapon() && temp.isAugmented()) ? 0x0000FFFF & temp.getAugmentation().getAugmentationId() : 0);
			writeD((temp.isWeapon() && temp.isAugmented()) ? temp.getAugmentation().getAugmentationId() >> 16 : 0);
		}
	}
}