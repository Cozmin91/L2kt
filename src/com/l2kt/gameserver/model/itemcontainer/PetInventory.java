package com.l2kt.gameserver.model.itemcontainer;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.model.actor.instance.Pet;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.type.EtcItemType;

public class PetInventory extends Inventory
{
	private final Pet _owner;
	
	public PetInventory(Pet owner)
	{
		_owner = owner;
	}
	
	@Override
	public Pet getOwner()
	{
		return _owner;
	}
	
	@Override
	public int getOwnerId()
	{
		int id;
		try
		{
			id = _owner.getOwner().getObjectId();
		}
		catch (NullPointerException e)
		{
			return 0;
		}
		return id;
	}
	
	@Override
	protected void refreshWeight()
	{
		super.refreshWeight();
		
		getOwner().updateAndBroadcastStatus(1);
		getOwner().sendPetInfosToOwner();
	}
	
	public boolean validateCapacity(ItemInstance item)
	{
		int slots = 0;
		
		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != EtcItemType.HERB)
			slots++;
		
		return validateCapacity(slots);
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return _items.size() + slots <= _owner.getInventoryLimit();
	}
	
	public boolean validateWeight(ItemInstance item, int count)
	{
		return validateWeight(count * item.getItem().getWeight());
	}
	
	@Override
	public boolean validateWeight(int weight)
	{
		return _totalWeight + weight <= _owner.getMaxLoad();
	}
	
	@Override
	protected ItemInstance.ItemLocation getBaseLocation()
	{
		return ItemInstance.ItemLocation.PET;
	}
	
	@Override
	protected ItemInstance.ItemLocation getEquipLocation()
	{
		return ItemInstance.ItemLocation.PET_EQUIP;
	}
	
	@Override
	public void deleteMe()
	{
		final Player petOwner = getOwner().getOwner();
		if (petOwner != null)
		{
			for (ItemInstance item : _items)
			{
				if (petOwner.getInventory().validateCapacity(1))
					getOwner().transferItem("return", item.getObjectId(), item.getCount(), petOwner.getInventory(), petOwner, getOwner());
				else
				{
					final ItemInstance droppedItem = dropItem("drop", item.getObjectId(), item.getCount(), petOwner, getOwner());
					droppedItem.dropMe(getOwner(), getOwner().getX() + Rnd.INSTANCE.get(-70, 70), getOwner().getY() + Rnd.INSTANCE.get(-70, 70), getOwner().getZ() + 30);
				}
				
			}
		}
		_items.clear();
	}
}