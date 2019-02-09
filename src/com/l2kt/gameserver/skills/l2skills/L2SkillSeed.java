package com.l2kt.gameserver.skills.l2skills;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.skills.effects.EffectSeed;
import com.l2kt.gameserver.templates.StatsSet;
import com.l2kt.gameserver.templates.skills.L2EffectType;

public class L2SkillSeed extends L2Skill
{
	public L2SkillSeed(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		// Update Seeds Effects
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			EffectSeed oldEffect = (EffectSeed) target.getFirstEffect(getId());
			if (oldEffect == null)
				getEffects(caster, target);
			else
				oldEffect.increasePower();
			
			L2Effect[] effects = target.getAllEffects();
			for (L2Effect effect : effects)
				if (effect.getEffectType() == L2EffectType.SEED)
					effect.rescheduleEffect();
		}
	}
}