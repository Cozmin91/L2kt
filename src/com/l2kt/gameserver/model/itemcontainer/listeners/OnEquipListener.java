package com.l2kt.gameserver.model.itemcontainer.listeners;

import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.item.instance.ItemInstance;

public interface OnEquipListener
{
	public void onEquip(int slot, ItemInstance item, Playable actor);
	
	public void onUnequip(int slot, ItemInstance item, Playable actor);
}