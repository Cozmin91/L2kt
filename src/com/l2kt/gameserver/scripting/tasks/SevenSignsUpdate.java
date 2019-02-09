package com.l2kt.gameserver.scripting.tasks;

import com.l2kt.gameserver.scripting.ScheduledQuest;
import com.l2kt.gameserver.instancemanager.SevenSigns;
import com.l2kt.gameserver.instancemanager.SevenSignsFestival;

public final class SevenSignsUpdate extends ScheduledQuest
{
	public SevenSignsUpdate()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		if (!SevenSigns.getInstance().isSealValidationPeriod())
			SevenSignsFestival.getInstance().saveFestivalData(false);
		
		SevenSigns.getInstance().saveSevenSignsData();
		SevenSigns.getInstance().saveSevenSignsStatus();
	}
	
	@Override
	public final void onEnd()
	{
	}
}