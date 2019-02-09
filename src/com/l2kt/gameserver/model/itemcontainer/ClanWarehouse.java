package com.l2kt.gameserver.model.itemcontainer;

import com.l2kt.Config;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends ItemContainer
{
	private final Clan _clan;
	
	public ClanWarehouse(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	public String getName()
	{
		return "ClanWarehouse";
	}
	
	@Override
	public int getOwnerId()
	{
		return _clan.getClanId();
	}
	
	@Override
	public Player getOwner()
	{
		return _clan.getLeader().getPlayerInstance();
	}
	
	@Override
	public ItemInstance.ItemLocation getBaseLocation()
	{
		return ItemInstance.ItemLocation.CLANWH;
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return _items.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN;
	}
}