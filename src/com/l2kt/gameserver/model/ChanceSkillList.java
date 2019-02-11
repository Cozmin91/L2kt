package com.l2kt.gameserver.model;

import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.handler.ISkillHandler;
import com.l2kt.gameserver.handler.SkillHandler;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse;
import com.l2kt.gameserver.skills.effects.EffectChanceSkillTrigger;
import com.l2kt.gameserver.templates.skills.L2SkillType;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CT2.3: Added support for allowing effect as a chance skill trigger (DrHouse)
 * @author kombat
 */
public class ChanceSkillList extends ConcurrentHashMap<IChanceSkillTrigger, ChanceCondition>
{
	protected static final Logger _log = Logger.getLogger(ChanceSkillList.class.getName());
	private static final long serialVersionUID = 1L;
	
	private final Creature _owner;
	
	public ChanceSkillList(Creature owner)
	{
		super();
		_owner = owner;
	}
	
	public Creature getOwner()
	{
		return _owner;
	}
	
	public void onHit(Creature target, boolean ownerWasHit, boolean wasCrit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if (wasCrit)
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
		}
		else
		{
			event = ChanceCondition.EVT_HIT;
			if (wasCrit)
				event |= ChanceCondition.EVT_CRIT;
		}
		
		onChanceSkillEvent(event, target);
	}
	
	public void onEvadedHit(Creature attacker)
	{
		onChanceSkillEvent(ChanceCondition.EVT_EVADED_HIT, attacker);
	}
	
	public void onSkillHit(Creature target, boolean ownerWasHit, boolean wasMagic, boolean wasOffensive)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (wasOffensive)
			{
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
			}
			else
			{
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		}
		else
		{
			event = ChanceCondition.EVT_CAST;
			event |= wasMagic ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= wasOffensive ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}
		
		onChanceSkillEvent(event, target);
	}
	
	public void onStart()
	{
		onChanceSkillEvent(ChanceCondition.EVT_ON_START, _owner);
	}
	
	public void onActionTime()
	{
		onChanceSkillEvent(ChanceCondition.EVT_ON_ACTION_TIME, _owner);
	}
	
	public void onExit()
	{
		onChanceSkillEvent(ChanceCondition.EVT_ON_EXIT, _owner);
	}
	
	public void onChanceSkillEvent(int event, Creature target)
	{
		if (_owner.isDead())
			return;
		
		for (Map.Entry<IChanceSkillTrigger, ChanceCondition> entry : entrySet())
		{
			IChanceSkillTrigger trigger = entry.getKey();
			ChanceCondition cond = entry.getValue();
			
			if (cond != null && cond.trigger(event))
			{
				if (trigger instanceof L2Skill)
					makeCast((L2Skill) trigger, target);
				else if (trigger instanceof EffectChanceSkillTrigger)
					makeCast((EffectChanceSkillTrigger) trigger, target);
			}
		}
	}
	
	private void makeCast(L2Skill skill, Creature target)
	{
		try
		{
			if (skill.getWeaponDependancy(_owner) && skill.checkCondition(_owner, target, false))
			{
				if (skill.triggersChanceSkill()) // skill will trigger another skill, but only if its not chance skill
				{
					skill = SkillTable.INSTANCE.getInfo(skill.getTriggeredChanceId(), skill.getTriggeredChanceLevel());
					if (skill == null || skill.getSkillType() == L2SkillType.NOTDONE)
						return;
				}
				
				if (_owner.isSkillDisabled(skill))
					return;
				
				if (skill.getReuseDelay() > 0)
					_owner.disableSkill(skill, skill.getReuseDelay());
				
				WorldObject[] targets = skill.getTargetList(_owner, false, target);
				
				if (targets.length == 0)
					return;
				
				Creature firstTarget = (Creature) targets[0];
				
				_owner.broadcastPacket(new MagicSkillLaunched(_owner, skill.getId(), skill.getLevel(), Arrays.stream(targets).collect(Collectors.toList())));
				_owner.broadcastPacket(new MagicSkillUse(_owner, firstTarget, skill.getId(), skill.getLevel(), 0, 0));
				
				// Launch the magic skill and calculate its effects
				// TODO: once core will support all possible effects, use effects (not handler)
				final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
				if (handler != null)
					handler.useSkill(_owner, skill, targets);
				else
					skill.useSkill(_owner, targets);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
	
	private void makeCast(EffectChanceSkillTrigger effect, Creature target)
	{
		try
		{
			if (effect == null || !effect.triggersChanceSkill())
				return;
			
			L2Skill triggered = SkillTable.INSTANCE.getInfo(effect.getTriggeredChanceId(), effect.getTriggeredChanceLevel());
			if (triggered == null)
				return;
			Creature caster = triggered.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF ? _owner : effect.getEffector();
			
			if (caster == null || triggered.getSkillType() == L2SkillType.NOTDONE || caster.isSkillDisabled(triggered))
				return;
			
			if (triggered.getReuseDelay() > 0)
				caster.disableSkill(triggered, triggered.getReuseDelay());
			
			WorldObject[] targets = triggered.getTargetList(caster, false, target);
			
			if (targets.length == 0)
				return;
			
			Creature firstTarget = (Creature) targets[0];
			
			ISkillHandler handler = SkillHandler.getInstance().getHandler(triggered.getSkillType());
			
			_owner.broadcastPacket(new MagicSkillLaunched(_owner, triggered.getId(), triggered.getLevel(), Arrays.stream(targets).collect(Collectors.toList())));
			_owner.broadcastPacket(new MagicSkillUse(_owner, firstTarget, triggered.getId(), triggered.getLevel(), 0, 0));
			
			// Launch the magic skill and calculate its effects
			// TODO: once core will support all possible effects, use effects (not handler)
			if (handler != null)
				handler.useSkill(caster, triggered, targets);
			else
				triggered.useSkill(caster, targets);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
}