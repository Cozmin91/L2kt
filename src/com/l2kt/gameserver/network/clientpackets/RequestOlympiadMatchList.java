package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.commons.lang.StringUtil;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.olympiad.Olympiad;
import com.l2kt.gameserver.model.olympiad.OlympiadGameManager;
import com.l2kt.gameserver.model.olympiad.OlympiadGameTask;

import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestOlympiadMatchList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null || !activeChar.isInObserverMode())
			return;
		
		int i = 0;
		
		final StringBuilder sb = new StringBuilder(1500);
		for (OlympiadGameTask task : OlympiadGameManager.getInstance().getOlympiadTasks())
		{
			StringUtil.append(sb, "<tr><td fixwidth=10><a action=\"bypass arenachange ", i, "\">", ++i, "</a></td><td fixwidth=80>");
			
			if (task.isGameStarted())
			{
				if (task.isInTimerTime())
					StringUtil.append(sb, "&$907;"); // Counting In Progress
				else if (task.isBattleStarted())
					StringUtil.append(sb, "&$829;"); // In Progress
				else
					StringUtil.append(sb, "&$908;"); // Terminate
					
				StringUtil.append(sb, "</td><td>", task.getGame().getPlayerNames()[0], "&nbsp; / &nbsp;", task.getGame().getPlayerNames()[1]);
			}
			else
				StringUtil.append(sb, "&$906;", "</td><td>&nbsp;"); // Initial State
				
			StringUtil.append(sb, "</td><td><font color=\"aaccff\"></font></td></tr>");
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_arena_observe_list.htm");
		html.replace("%list%", sb.toString());
		activeChar.sendPacket(html);
	}
}