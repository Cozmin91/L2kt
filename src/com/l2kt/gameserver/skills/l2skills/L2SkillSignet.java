package com.l2kt.gameserver.skills.l2skills;

import com.l2kt.gameserver.data.xml.NpcData;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.EffectPoint;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.location.Location;
import com.l2kt.gameserver.idfactory.IdFactory;
import com.l2kt.gameserver.templates.StatsSet;

public final class L2SkillSignet extends L2Skill
{
	private final int _effectNpcId;
	public int effectId;
	
	public L2SkillSignet(StatsSet set)
	{
		super(set);
		_effectNpcId = set.getInteger("effectNpcId", -1);
		effectId = set.getInteger("effectId", -1);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		NpcTemplate template = NpcData.getInstance().getTemplate(_effectNpcId);
		EffectPoint effectPoint = new EffectPoint(IdFactory.getInstance().getNextId(), template, caster);
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		
		Location worldPosition = null;
		if (caster instanceof Player && getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
			worldPosition = ((Player) caster).getCurrentSkillWorldPosition();
		
		getEffects(caster, effectPoint);
		
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe((worldPosition != null) ? worldPosition : caster.getPosition());
	}
}