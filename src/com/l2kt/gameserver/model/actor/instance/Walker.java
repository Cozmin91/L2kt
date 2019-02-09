package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.ai.type.CreatureAI;
import com.l2kt.gameserver.model.actor.ai.type.WalkerAI;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;

/**
 * A Walker is a {@link Folk} which continuously walks, following a defined route. It got no other Intention than MOVE_TO, so it never stops walking/running - except for programmed timers - and can speak.<br>
 * <br>
 * It can't be killed, and the AI is never detached (works even when region is supposed to be down due to lack of players).
 */
public class Walker extends Folk
{
	public Walker(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		setAI(new WalkerAI(this));
	}
	
	@Override
	public void setAI(CreatureAI newAI)
	{
		// AI can't be detached, npc must move with the same AI instance.
		if (!(_ai instanceof WalkerAI))
			_ai = newAI;
	}
	
	@Override
	public void reduceCurrentHp(double i, Creature attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		return false;
	}
	
	@Override
	public WalkerAI getAI()
	{
		return (WalkerAI) _ai;
	}
	
	@Override
	public void detachAI()
	{
		// AI can't be detached.
	}
}