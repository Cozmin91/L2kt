package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.ShotType;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.Summon;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.util.Broadcast;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * Beast SoulShot Handler
 * @author Tempy
 */
public class BeastSoulShot implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (playable == null)
			return;
		
		final Player activeOwner = playable.getActingPlayer();
		if (activeOwner == null)
			return;
		
		if (playable instanceof Summon)
		{
			activeOwner.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return;
		}
		
		final Summon activePet = activeOwner.getPet();
		if (activePet == null)
		{
			activeOwner.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return;
		}
		
		if (activePet.isDead())
		{
			activeOwner.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET);
			return;
		}
		
		// SoulShots are already active.
		if (activePet.isChargedShot(ShotType.SOULSHOT))
			return;
		
		// If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
		if (!activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), activePet.getSoulShotsPerHit(), null, false))
		{
			if (!activeOwner.disableAutoShot(item.getItemId()))
				activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
			return;
		}
		
		activeOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addItemName(item.getItemId()));
		activePet.setChargedShot(ShotType.SOULSHOT, true);
		Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(activePet, activePet, 2033, 1, 0, 0), 600);
	}
}