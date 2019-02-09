package com.l2kt.gameserver.scripting.scripts.ai.group;

import com.l2kt.Config;
import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.location.Location;
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript;

/**
 * A fleeing NPC.<br>
 * <br>
 * His behavior is to always flee, and never attack.
 */
public class FleeingNPCs extends L2AttackableAIScript
{
	public FleeingNPCs()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(20432);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		// Calculate random coords.
		final int rndX = npc.getX() + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		final int rndY = npc.getY() + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		
		// Wait the NPC to be immobile to move him again. Also check destination point.
		if (!npc.isMoving() && GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), rndX, rndY, npc.getZ()))
			npc.getAI().setIntention(CtrlIntention.MOVE_TO, new Location(rndX, rndY, npc.getZ()));
		
		return null;
	}
}