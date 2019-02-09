package com.l2kt.gameserver.scripting.scripts.ai.group;

import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Attackable;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.base.Sex;
import com.l2kt.gameserver.scripting.EventType;
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import com.l2kt.gameserver.templates.skills.L2SkillType;

/**
 * This script holds MoS monsters behavior. If they see you with an equipped weapon, they will speak and attack you.
 */
public class Monastery extends L2AttackableAIScript
{
	private static final int[] BROTHERS_SEEKERS_MONKS =
	{
		22124,
		22125,
		22126,
		22127,
		22129
	};
	
	private static final int[] GUARDIANS_BEHOLDERS =
	{
		22134,
		22135
	};
	
	public Monastery()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(BROTHERS_SEEKERS_MONKS, EventType.ON_AGGRO, EventType.ON_SPAWN, EventType.ON_SPELL_FINISHED);
		addEventIds(GUARDIANS_BEHOLDERS, EventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		if (!npc.isInCombat())
		{
			if (player.getActiveWeaponInstance() != null)
			{
				npc.setTarget(player);
				npc.broadcastNpcSay(((player.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + player.getName() + ", move your weapon away!");
				
				switch (npc.getNpcId())
				{
					case 22124:
					case 22126:
						npc.doCast(SkillTable.getInstance().getInfo(4589, 8));
						break;
					
					default:
						attack(((Attackable) npc), player);
						break;
				}
			}
			else if (((Attackable) npc).getMostHated() == null)
				return null;
		}
		return super.onAggro(npc, player, isPet);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		if (skill.getSkillType() == L2SkillType.AGGDAMAGE && targets.length != 0)
		{
			for (WorldObject obj : targets)
			{
				if (obj.equals(npc))
				{
					npc.broadcastNpcSay(((caster.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + caster.getName() + ", move your weapon away!");
					attack(((Attackable) npc), caster);
					break;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		for (Player target : npc.getKnownTypeInRadius(Player.class, npc.getTemplate().getAggroRange()))
		{
			if (!target.isDead() && GeoEngine.getInstance().canSeeTarget(npc, target))
			{
				if (target.getActiveWeaponInstance() != null && !npc.isInCombat() && npc.getTarget() == null)
				{
					npc.setTarget(target);
					npc.broadcastNpcSay(((target.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + target.getName() + ", move your weapon away!");
					
					switch (npc.getNpcId())
					{
						case 22124:
						case 22126:
						case 22127:
							npc.doCast(SkillTable.getInstance().getInfo(4589, 8));
							break;
						
						default:
							attack(((Attackable) npc), target);
							break;
					}
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, L2Skill skill)
	{
		if (skill.getId() == 4589)
			attack(((Attackable) npc), player);
		
		return super.onSpellFinished(npc, player, skill);
	}
}