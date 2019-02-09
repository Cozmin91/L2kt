package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.gameserver.model.base.ClassId;
import com.l2kt.gameserver.model.base.ClassRace;
import com.l2kt.gameserver.model.base.ClassType;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;

public final class VillageMasterFighter extends VillageMaster
{
	public VillageMasterFighter(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected final boolean checkVillageMasterRace(ClassId pclass)
	{
		if (pclass == null)
			return false;
		
		return pclass.getRace() == ClassRace.HUMAN || pclass.getRace() == ClassRace.ELF;
	}
	
	@Override
	protected final boolean checkVillageMasterTeachType(ClassId pclass)
	{
		if (pclass == null)
			return false;
		
		return pclass.getType() == ClassType.FIGHTER;
	}
}