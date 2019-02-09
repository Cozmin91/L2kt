package com.l2kt.gameserver.scripting.tasks;

import com.l2kt.gameserver.data.sql.ClanTable;
import com.l2kt.gameserver.model.pledge.Clan;
import com.l2kt.gameserver.model.pledge.ClanMember;
import com.l2kt.gameserver.scripting.ScheduledQuest;

public class ClanLeaderTransfer extends ScheduledQuest
{
	public ClanLeaderTransfer()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getNewLeaderId() <= 0)
				continue;
			
			final ClanMember member = clan.getClanMember(clan.getNewLeaderId());
			if (member == null)
			{
				clan.setNewLeaderId(0, true);
				continue;
			}
			
			clan.setNewLeader(member);
		}
	}
	
	@Override
	public final void onEnd()
	{
	}
}