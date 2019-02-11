package com.l2kt.gameserver.scripting.scripts.ai.group;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.model.L2Effect;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class HotSpringDisease extends L2AttackableAIScript
{
	// Diseases
	private static final int MALARIA = 4554;
	
	// Chance
	private static final int DISEASE_CHANCE = 1;
	
	// Monsters
	private static final int[][] MONSTERS_DISEASES =
	{
		{
			21314,
			21316,
			21317,
			21319,
			21321,
			21322
		},
		{
			4551,
			4552,
			4553,
			4552,
			4551,
			4553
		}
	};
	
	public HotSpringDisease()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackActId(MONSTERS_DISEASES[0]);
	}
	
	@Override
	public String onAttackAct(Npc npc, Player victim)
	{
		for (int i = 0; i < 6; i++)
		{
			if (MONSTERS_DISEASES[0][i] != npc.getNpcId())
				continue;
			
			tryToApplyEffect(npc, victim, MALARIA);
			tryToApplyEffect(npc, victim, MONSTERS_DISEASES[1][i]);
		}
		return super.onAttackAct(npc, victim);
	}
	
	private static void tryToApplyEffect(Npc npc, Player victim, int skillId)
	{
		if (Rnd.INSTANCE.get(100) < DISEASE_CHANCE)
		{
			int level = 1;
			
			L2Effect[] effects = victim.getAllEffects();
			if (effects.length != 0)
			{
				for (L2Effect e : effects)
				{
					if (e.getSkill().getId() != skillId)
						continue;
					
					level += e.getSkill().getLevel();
					e.exit();
				}
			}
			
			if (level > 10)
				level = 10;
			
			SkillTable.INSTANCE.getInfo(skillId, level).getEffects(npc, victim);
		}
	}
}