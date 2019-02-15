package com.l2kt.gameserver.scripting.tasks;

import com.l2kt.gameserver.data.sql.ClanTable;
import com.l2kt.gameserver.scripting.ScheduledQuest;

public final class ClanLadderRefresh extends ScheduledQuest
{
	public ClanLadderRefresh()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		ClanTable.INSTANCE.refreshClansLadder(true);
	}
	
	@Override
	public final void onEnd()
	{
	}
}