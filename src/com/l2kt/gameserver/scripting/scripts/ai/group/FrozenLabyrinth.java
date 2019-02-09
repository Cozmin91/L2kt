package com.l2kt.gameserver.scripting.scripts.ai.group;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Attackable;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript;

/**
 * Frozen Labyrinth<br>
 * Those mobs split if you use physical attacks on them.
 */
public final class FrozenLabyrinth extends L2AttackableAIScript
{
	private static final int PRONGHORN_SPIRIT = 22087;
	private static final int PRONGHORN = 22088;
	private static final int LOST_BUFFALO = 22093;
	private static final int FROST_BUFFALO = 22094;
	
	public FrozenLabyrinth()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addSkillSeeId(PRONGHORN, FROST_BUFFALO);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		// Offensive physical skill casted on npc.
		if (skill != null && !skill.isMagic() && skill.isOffensive() && targets[0] == npc)
		{
			int spawnId = LOST_BUFFALO;
			if (npc.getNpcId() == PRONGHORN)
				spawnId = PRONGHORN_SPIRIT;
			
			int diff = 0;
			for (int i = 0; i < Rnd.get(6, 8); i++)
			{
				int x = diff < 60 ? npc.getX() + diff : npc.getX();
				int y = diff >= 60 ? npc.getY() + (diff - 40) : npc.getY();
				
				final Attackable monster = (Attackable) addSpawn(spawnId, x, y, npc.getZ(), npc.getHeading(), false, 120000, false);
				attack(monster, caster);
				diff += 20;
			}
			npc.deleteMe();
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
}