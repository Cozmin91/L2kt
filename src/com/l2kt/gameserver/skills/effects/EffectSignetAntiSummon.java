package com.l2kt.gameserver.skills.effects;

import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.ai.CtrlEvent;
import com.l2kt.gameserver.model.actor.instance.EffectPoint;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.skills.Env;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;
import com.l2kt.gameserver.templates.skills.L2EffectType;

/**
 * @author Forsaiken
 */
public class EffectSignetAntiSummon extends L2Effect
{
	private EffectPoint _actor;
	
	public EffectSignetAntiSummon(Env env, EffectTemplate template)
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
		_actor = (EffectPoint) getEffected();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() == getTotalCount() - 1)
			return true; // do nothing first time
			
		final int mpConsume = getSkill().getMpConsume();
		final Player caster = (Player) getEffector();
		
		for (Playable cha : _actor.getKnownTypeInRadius(Playable.class, getSkill().getSkillRadius()))
		{
			if (!caster.canAttackCharacter(cha))
				continue;
			
			final Player owner = cha.getActingPlayer();
			if (owner != null && owner.getPet() != null)
			{
				if (mpConsume > getEffector().getCurrentMp())
				{
					getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
					return false;
				}
				getEffector().reduceCurrentMp(mpConsume);
				
				owner.getPet().unSummon(owner);
				owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
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