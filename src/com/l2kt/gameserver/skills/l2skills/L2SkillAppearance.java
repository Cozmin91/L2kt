package com.l2kt.gameserver.skills.l2skills;

import java.util.logging.Level;

import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.templates.StatsSet;

public class L2SkillAppearance extends L2Skill
{
	private final int _faceId;
	private final int _hairColorId;
	private final int _hairStyleId;
	
	public L2SkillAppearance(StatsSet set)
	{
		super(set);
		
		_faceId = set.getInteger("faceId", -1);
		_hairColorId = set.getInteger("hairColorId", -1);
		_hairStyleId = set.getInteger("hairStyleId", -1);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		try
		{
			for (WorldObject target : targets)
			{
				if (target instanceof Player)
				{
					Player targetPlayer = (Player) target;
					if (_faceId >= 0)
						targetPlayer.getAppearance().setFace(_faceId);
					if (_hairColorId >= 0)
						targetPlayer.getAppearance().setHairColor(_hairColorId);
					if (_hairStyleId >= 0)
						targetPlayer.getAppearance().setHairStyle(_hairStyleId);
					
					targetPlayer.broadcastUserInfo();
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}
}