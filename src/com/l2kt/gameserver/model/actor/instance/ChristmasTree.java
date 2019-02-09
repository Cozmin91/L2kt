package com.l2kt.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.zone.ZoneId;

import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;

/**
 * Christmas trees used on events.<br>
 * The special tree (npcId 13007) emits a regen aura, but only when set outside a peace zone.
 */
public class ChristmasTree extends Folk
{
	public static final int SPECIAL_TREE_ID = 13007;
	
	private ScheduledFuture<?> _aiTask;
	
	public ChristmasTree(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		if (template.getNpcId() == SPECIAL_TREE_ID && !isInsideZone(ZoneId.TOWN))
		{
			final L2Skill recoveryAura = SkillTable.FrequentSkill.SPECIAL_TREE_RECOVERY_BONUS.getSkill();
			if (recoveryAura == null)
				return;
			
			_aiTask = ThreadPool.scheduleAtFixedRate(() ->
			{
				for (Player player : getKnownTypeInRadius(Player.class, 200))
				{
					if (player.getFirstEffect(recoveryAura) == null)
						recoveryAura.getEffects(player, player);
				}
			}, 3000, 3000);
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
		super.deleteMe();
	}
	
	@Override
	public void onAction(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}