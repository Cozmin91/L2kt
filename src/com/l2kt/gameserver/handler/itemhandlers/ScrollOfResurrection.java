package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.gameserver.data.manager.CastleManager;
import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Pet;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.entity.Siege;
import com.l2kt.gameserver.model.holder.IntIntHolder;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.SystemMessageId;

public class ScrollOfResurrection implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		
		if (activeChar.isMovementDisabled())
			return;
		
		final Creature target = (Creature) activeChar.getTarget();
		
		// Target must be a dead pet or player.
		if ((!(target instanceof Pet) && !(target instanceof Player)) || !target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		// Pet scrolls to ress a player.
		if (item.getItemId() == 6387 && target instanceof Player)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		// Pickup player, or pet owner in case target is a pet.
		final Player targetPlayer = target.getActingPlayer();
		
		// Check if target isn't in a active siege zone.
		final Siege siege = CastleManager.INSTANCE.getActiveSiege(targetPlayer);
		if (siege != null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
			return;
		}
		
		// Check if the target is in a festival.
		if (targetPlayer.isFestivalParticipant())
			return;
		
		if (targetPlayer.isReviveRequested())
		{
			if (targetPlayer.isRevivingPet())
				activeChar.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
			else
				activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
				
			return;
		}
		
		final IntIntHolder[] skills = item.getEtcItem().getSkills();
		if (skills == null)
		{
			LOGGER.warn("{} doesn't have any registered skill for handler.", item.getName());
			return;
		}
		
		for (IntIntHolder skillInfo : skills)
		{
			if (skillInfo == null)
				continue;
			
			final L2Skill itemSkill = skillInfo.getSkill();
			if (itemSkill == null)
				continue;
			
			// Scroll consumption is made on skill call, not on item call.
			playable.useMagic(itemSkill, false, false);
		}
	}
}