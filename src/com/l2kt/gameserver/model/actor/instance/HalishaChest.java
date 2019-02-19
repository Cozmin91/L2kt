package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.gameserver.model.actor.template.NpcTemplate;

public final class HalishaChest extends Monster
{
	public HalishaChest(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		setNoRndWalk(true);
		setShowSummonAnimation(true);
		disableCoreAI(true);
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}