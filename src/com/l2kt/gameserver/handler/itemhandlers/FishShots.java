package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.ShotType;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.holder.IntIntHolder;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.kind.Weapon;
import com.l2kt.gameserver.model.item.type.WeaponType;
import com.l2kt.gameserver.util.Broadcast;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author -Nemesiss-
 */
public class FishShots implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		final ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final Weapon weaponItem = activeChar.getActiveWeaponItem();
		
		if (weaponInst == null || weaponItem.getItemType() != WeaponType.FISHINGROD)
			return;
		
		// Fishshot is already active
		if (activeChar.isChargedShot(ShotType.FISH_SOULSHOT))
			return;
		
		// Wrong grade of soulshot for that fishing pole.
		if (weaponItem.getCrystalType() != item.getItem().getCrystalType())
		{
			activeChar.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE);
			return;
		}
		
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false))
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
			return;
		}
		
		final IntIntHolder[] skills = item.getItem().getSkills();
		
		activeChar.setChargedShot(ShotType.FISH_SOULSHOT, true);
		Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, skills[0].getId(), 1, 0, 0));
	}
}