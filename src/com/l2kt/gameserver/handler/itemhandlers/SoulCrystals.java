package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.holder.IntIntHolder;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.kind.EtcItem;

/**
 * Template for item skills handler.
 * @author Hasha
 */
public class SoulCrystals implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final EtcItem etcItem = item.getEtcItem();
		
		final IntIntHolder[] skills = etcItem.getSkills();
		if (skills == null)
			return;
		
		final L2Skill itemSkill = skills[0].getSkill();
		if (itemSkill == null || itemSkill.getId() != 2096)
			return;
		
		final Player player = (Player) playable;
		
		if (player.isCastingNow())
			return;
		
		if (!itemSkill.checkCondition(player, player.getTarget(), false))
			return;
		
		// No message on retail, the use is just forgotten.
		if (player.isSkillDisabled(itemSkill))
			return;
		
		player.getAI().setIntention(CtrlIntention.IDLE);
		if (!player.useMagic(itemSkill, forceUse, false))
			return;
		
		int reuseDelay = itemSkill.getReuseDelay();
		if (etcItem.getReuseDelay() > reuseDelay)
			reuseDelay = etcItem.getReuseDelay();
		
		player.addTimeStamp(itemSkill, reuseDelay);
		if (reuseDelay != 0)
			player.disableSkill(itemSkill, reuseDelay);
	}
}