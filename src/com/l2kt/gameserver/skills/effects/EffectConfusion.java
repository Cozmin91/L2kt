package com.l2kt.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Attackable;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.instance.Chest;
import com.l2kt.gameserver.model.actor.instance.Door;
import com.l2kt.gameserver.skills.Env;

import com.l2kt.gameserver.templates.skills.L2EffectFlag;
import com.l2kt.gameserver.templates.skills.L2EffectType;

/**
 * This effect changes the target of the victim. It adds some random aggro aswell to force the monster to keep attacking. As the added aggro is random, the victim can often change of target.<br>
 * <br>
 * Any character can fill the aggroList of the victim. For a specialized use, consider using EffectConfuseMob.
 * @author littlecrow, Tryskell
 */
public class EffectConfusion extends L2Effect
{
	public EffectConfusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CONFUSION;
	}
	
	/** Notify started */
	@Override
	public boolean onStart()
	{
		getEffected().startConfused();
		onActionTime();
		return true;
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		getEffected().stopConfused(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		List<Creature> targetList = new ArrayList<>();
		
		// Getting the possible targets
		for (WorldObject obj : getEffected().getKnownType(WorldObject.class))
		{
			// Attackable NPCs and playable characters (players, summons) are put in the list.
			if ((obj instanceof Attackable || obj instanceof Playable) && (obj != getEffected()))
				// Don't put doors nor chests on it.
				if (!(obj instanceof Door || obj instanceof Chest))
					targetList.add((Creature) obj);
		}
		
		// if there is no target, exit function
		if (targetList.isEmpty())
			return true;
		
		// Choosing randomly a new target
		WorldObject target = Rnd.get(targetList);
		
		// Attacking the target
		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.ATTACK, target);
		
		// Add aggro to that target aswell. The aggro power is random.
		int aggro = (5 + Rnd.get(5)) * getEffector().getLevel();
		((Attackable) getEffected()).addDamageHate((Creature) target, 0, aggro);
		
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.CONFUSED.getMask();
	}
}