package com.l2kt.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.EffectPoint;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;
import com.l2kt.gameserver.skills.l2skills.L2SkillSignet;
import com.l2kt.gameserver.skills.l2skills.L2SkillSignetCasttime;
import com.l2kt.gameserver.templates.skills.L2EffectType;

/**
 * @authors Forsaiken, Sami
 */
public class EffectSignet extends L2Effect
{
	private L2Skill _skill;
	private EffectPoint _actor;
	private boolean _srcInArena;
	
	public EffectSignet(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_EFFECT;
	}
	
	@Override
	public boolean onStart()
	{
		if (getSkill() instanceof L2SkillSignet)
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignet) getSkill()).effectId, getLevel());
		else if (getSkill() instanceof L2SkillSignetCasttime)
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime) getSkill()).effectId, getLevel());
		
		_actor = (EffectPoint) getEffected();
		_srcInArena = getEffector().isInArena();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (_skill == null)
			return true;
		
		int mpConsume = _skill.getMpConsume();
		if (mpConsume > getEffector().getCurrentMp())
		{
			getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		}
		getEffector().reduceCurrentMp(mpConsume);
		
		List<Creature> targets = new ArrayList<>();
		for (Creature cha : _actor.getKnownTypeInRadius(Creature.class, getSkill().getSkillRadius()))
		{
			if (_skill.isOffensive() && !L2Skill.checkForAreaOffensiveSkills(getEffector(), cha, _skill, _srcInArena))
				continue;
			
			// there doesn't seem to be a visible effect with MagicSkillLaunched packet...
			_actor.broadcastPacket(new MagicSkillUse(_actor, cha, _skill.getId(), _skill.getLevel(), 0, 0));
			targets.add(cha);
		}
		
		if (!targets.isEmpty())
			getEffector().callSkill(_skill, targets.toArray(new Creature[targets.size()]));
		
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
			_actor.deleteMe();
	}
}