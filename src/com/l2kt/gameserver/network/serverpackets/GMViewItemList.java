package com.l2kt.gameserver.network.serverpackets;

import java.util.Set;

import com.l2kt.gameserver.model.actor.instance.Pet;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.kind.Item;

public class GMViewItemList extends L2GameServerPacket
{
	private final Set<ItemInstance> _items;
	private final int _limit;
	private final String _playerName;
	
	public GMViewItemList(Player cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
	}
	
	public GMViewItemList(Pet cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x94);
		writeS(_playerName);
		writeD(_limit);
		writeH(0x01); // show window ??
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
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(item.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeD((temp.isAugmented()) ? temp.getAugmentation().getAugmentationId() : 0x00);
			writeD(temp.getMana());
		}
	}
}