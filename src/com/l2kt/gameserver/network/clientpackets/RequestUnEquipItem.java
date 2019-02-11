package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.kind.Item;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * format: cd
 */
public class RequestUnEquipItem extends L2GameClientPacket
{
	private int _slot;
	
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
		if (item == null)
			return;
		
		// Prevent of unequiping a cursed weapon
		if (_slot == Item.SLOT_LR_HAND && activeChar.isCursedWeaponEquipped())
			return;
		
		// Prevent player from unequipping items in special conditions
		if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAfraid() || activeChar.isAlikeDead())
		{
			activeChar.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			return;
		}
		
		if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
			return;
		
		ItemInstance[] unequipped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot);
		
		// show the update in the inventory
		InventoryUpdate iu = new InventoryUpdate();
		for (ItemInstance itm : unequipped)
		{
			itm.unChargeAllShots();
			iu.addModifiedItem(itm);
		}
		activeChar.sendPacket(iu);
		activeChar.broadcastUserInfo();
		
		// this can be 0 if the user pressed the right mousebutton twice very fast
		if (unequipped.length > 0)
		{
			SystemMessage sm = null;
			if (unequipped[0].getEnchantLevel() > 0)
			{
				sm = SystemMessage.Companion.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(unequipped[0].getEnchantLevel());
				sm.addItemName(unequipped[0]);
			}
			else
			{
				sm = SystemMessage.Companion.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(unequipped[0]);
			}
			activeChar.sendPacket(sm);
		}
	}
}