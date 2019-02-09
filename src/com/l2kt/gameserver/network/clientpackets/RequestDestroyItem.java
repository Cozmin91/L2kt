package com.l2kt.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.l2kt.L2DatabaseFactory;
import com.l2kt.gameserver.data.manager.CursedWeaponManager;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.type.EtcItemType;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate;

public final class RequestDestroyItem extends L2GameClientPacket
{
	private static final String DELETE_PET = "DELETE FROM pets WHERE item_obj_id=?";
	
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
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isProcessingTransaction() || player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		final ItemInstance itemToRemove = player.getInventory().getItemByObjectId(_objectId);
		if (itemToRemove == null)
			return;
		
		if (_count < 1 || _count > itemToRemove.getCount())
		{
			player.sendPacket(SystemMessageId.CANNOT_DESTROY_NUMBER_INCORRECT);
			return;
		}
		
		if (!itemToRemove.isStackable() && _count > 1)
			return;
		
		final int itemId = itemToRemove.getItemId();
		
		// Cannot discard item that the skill is consumming
		if (player.isCastingNow() && player.getCurrentSkill().getSkill() != null && player.getCurrentSkill().getSkill().getItemConsumeId() == itemId)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		// Cannot discard item that the skill is consuming
		if (player.isCastingSimultaneouslyNow() && player.getLastSimultaneousSkillCast() != null && player.getLastSimultaneousSkillCast().getItemConsumeId() == itemId)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if (!itemToRemove.isDestroyable() || CursedWeaponManager.getInstance().isCursed(itemId))
		{
			player.sendPacket((itemToRemove.isHeroItem()) ? SystemMessageId.HERO_WEAPONS_CANT_DESTROYED : SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if (itemToRemove.isEquipped() && (!itemToRemove.isStackable() || (itemToRemove.isStackable() && _count >= itemToRemove.getCount())))
		{
			final ItemInstance[] unequipped = player.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			final InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance item : unequipped)
			{
				item.unChargeAllShots();
				iu.addModifiedItem(item);
			}
			
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		
		// if it's a pet control item.
		if (itemToRemove.getItemType() == EtcItemType.PET_COLLAR)
		{
			// See if pet or mount is active ; can't destroy item linked to that pet.
			if ((player.getPet() != null && player.getPet().getControlItemId() == _objectId) || (player.isMounted() && player.getMountObjectId() == _objectId))
			{
				player.sendPacket(SystemMessageId.PET_SUMMONED_MAY_NOT_DESTROYED);
				return;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(DELETE_PET))
			{
				ps.setInt(1, _objectId);
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't delete pet item with objectid {}.", e, _objectId);
			}
		}
		
		player.destroyItem("Destroy", _objectId, _count, player, true);
	}
}