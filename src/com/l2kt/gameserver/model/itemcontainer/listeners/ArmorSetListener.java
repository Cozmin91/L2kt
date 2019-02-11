package com.l2kt.gameserver.model.itemcontainer.listeners;

import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.data.xml.ArmorSetData;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.ArmorSet;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.itemcontainer.Inventory;

public class ArmorSetListener implements OnEquipListener
{
	private static ArmorSetListener instance = new ArmorSetListener();
	
	public static ArmorSetListener getInstance()
	{
		return instance;
	}
	
	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if (!item.isEquipable())
			return;
		
		final Player player = (Player) actor;
		
		// Checks if player is wearing a chest item
		final ItemInstance chestItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if (chestItem == null)
			return;
		
		// checks if there is armorset for chest item that player worns
		final ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
		if (armorSet == null)
			return;
		
		// checks if equipped item is part of set
		if (armorSet.containItem(slot, item.getItemId()))
		{
			if (armorSet.containAll(player))
			{
				L2Skill skill = SkillTable.INSTANCE.getInfo(armorSet.getSkillId(), 1);
				if (skill != null)
				{
					player.addSkill(SkillTable.INSTANCE.getInfo(3006, 1), false);
					player.addSkill(skill, false);
					player.sendSkillList();
				}
				
				if (armorSet.containShield(player)) // has shield from set
				{
					L2Skill skills = SkillTable.INSTANCE.getInfo(armorSet.getShieldSkillId(), 1);
					if (skills != null)
					{
						player.addSkill(skills, false);
						player.sendSkillList();
					}
				}
				
				if (armorSet.isEnchanted6(player)) // has all parts of set enchanted to 6 or more
				{
					int skillId = armorSet.getEnchant6skillId();
					if (skillId > 0)
					{
						L2Skill skille = SkillTable.INSTANCE.getInfo(skillId, 1);
						if (skille != null)
						{
							player.addSkill(skille, false);
							player.sendSkillList();
						}
					}
				}
			}
		}
		else if (armorSet.containShield(item.getItemId()))
		{
			if (armorSet.containAll(player))
			{
				L2Skill skills = SkillTable.INSTANCE.getInfo(armorSet.getShieldSkillId(), 1);
				if (skills != null)
				{
					player.addSkill(skills, false);
					player.sendSkillList();
				}
			}
		}
	}
	
	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		final Player player = (Player) actor;
		
		boolean remove = false;
		int removeSkillId1 = 0; // set skill
		int removeSkillId2 = 0; // shield skill
		int removeSkillId3 = 0; // enchant +6 skill
		
		if (slot == Inventory.PAPERDOLL_CHEST)
		{
			final ArmorSet armorSet = ArmorSetData.getInstance().getSet(item.getItemId());
			if (armorSet == null)
				return;
			
			remove = true;
			removeSkillId1 = armorSet.getSkillId();
			removeSkillId2 = armorSet.getShieldSkillId();
			removeSkillId3 = armorSet.getEnchant6skillId();
		}
		else
		{
			final ItemInstance chestItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chestItem == null)
				return;
			
			final ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
			if (armorSet == null)
				return;
			
			if (armorSet.containItem(slot, item.getItemId())) // removed part of set
			{
				remove = true;
				removeSkillId1 = armorSet.getSkillId();
				removeSkillId2 = armorSet.getShieldSkillId();
				removeSkillId3 = armorSet.getEnchant6skillId();
			}
			else if (armorSet.containShield(item.getItemId())) // removed shield
			{
				remove = true;
				removeSkillId2 = armorSet.getShieldSkillId();
			}
		}
		
		if (remove)
		{
			if (removeSkillId1 != 0)
			{
				player.removeSkill(3006, false);
				player.removeSkill(removeSkillId1, false);
			}
			
			if (removeSkillId2 != 0)
				player.removeSkill(removeSkillId2, false);
			
			if (removeSkillId3 != 0)
				player.removeSkill(removeSkillId3, false);
			
			player.sendSkillList();
		}
	}
}