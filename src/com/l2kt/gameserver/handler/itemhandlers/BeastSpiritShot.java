package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.gameserver.extensions.BroadcastExtensionsKt;
import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.ShotType;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.Summon;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * Beast SpiritShot Handler
 * @author Tempy
 */
public class BeastSpiritShot implements IItemHandler
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
		
		final int itemId = item.getItemId();
		final boolean isBlessed = (itemId == 6647);
		
		// shots are already active.
		if (activePet.isChargedShot(isBlessed ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT))
			return;
		
		if (!activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), activePet.getSpiritShotsPerHit(), null, false))
		{
			if (!activeOwner.disableAutoShot(itemId))
				activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS_FOR_PET);
			return;
		}
		
		activeOwner.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.PET_USES_S1).addItemName(itemId));
		activePet.setChargedShot(isBlessed ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, true);
		BroadcastExtensionsKt.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(activePet, activePet, (isBlessed ? 2009 : 2008), 1, 0, 0), 600);
	}
}