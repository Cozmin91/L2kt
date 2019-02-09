package com.l2kt.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import com.l2kt.gameserver.data.xml.NpcData;
import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.ShotType;
import com.l2kt.gameserver.model.actor.Attackable;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.Summon;
import com.l2kt.gameserver.model.actor.ai.CtrlEvent;
import com.l2kt.gameserver.model.actor.instance.EffectPoint;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.location.Location;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.skills.Formulas;
import com.l2kt.gameserver.idfactory.IdFactory;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2kt.gameserver.skills.l2skills.L2SkillSignetCasttime;
import com.l2kt.gameserver.templates.skills.L2EffectType;

public class EffectSignetMDam extends L2Effect
{
	private EffectPoint _actor;
	
	public EffectSignetMDam(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}
	
	@Override
	public boolean onStart()
	{
		NpcTemplate template;
		if (getSkill() instanceof L2SkillSignetCasttime)
			template = NpcData.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
		else
			return false;
		
		EffectPoint effectPoint = new EffectPoint(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		
		Location worldPosition = null;
		if (getEffector() instanceof Player && getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
			worldPosition = ((Player) getEffector()).getCurrentSkillWorldPosition();
		
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe((worldPosition != null) ? worldPosition : getEffector().getPosition());
		
		_actor = effectPoint;
		return true;
		
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() >= getTotalCount() - 2)
			return true; // do nothing first 2 times
			
		final Player caster = (Player) getEffector();
		
		final int mpConsume = getSkill().getMpConsume();
		
		final boolean sps = caster.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bsps = caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		List<Creature> targets = new ArrayList<>();
		
		for (Creature cha : _actor.getKnownTypeInRadius(Creature.class, getSkill().getSkillRadius()))
		{
			if (cha == caster)
				continue;
			
			if (cha instanceof Attackable || cha instanceof Playable)
			{
				if (cha.isAlikeDead())
					continue;
				
				if (mpConsume > caster.getCurrentMp())
				{
					caster.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					return false;
				}
				
				caster.reduceCurrentMp(mpConsume);
				
				if (cha instanceof Playable)
				{
					if (caster.canAttackCharacter(cha))
					{
						targets.add(cha);
						caster.updatePvPStatus(cha);
					}
				}
				else
					targets.add(cha);
			}
		}
		
		if (!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), targets.toArray(new Creature[targets.size()])));
			for (Creature target : targets)
			{
				boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				byte shld = Formulas.calcShldUse(caster, target, getSkill());
				
				int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), shld, sps, bsps, mcrit);
				
				if (target instanceof Summon)
					target.broadcastStatusUpdate();
				
				if (mdam > 0)
				{
					// Manage cast break of the target (calculating rate, sending message...)
					Formulas.calcCastBreak(target, mdam);
					
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
			_actor.deleteMe();
	}
}