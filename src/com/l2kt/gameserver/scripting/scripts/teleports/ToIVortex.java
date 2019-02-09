package com.l2kt.gameserver.scripting.scripts.teleports;

import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.scripting.Quest;
import com.l2kt.gameserver.scripting.QuestState;

public class ToIVortex extends Quest
{
	private static final int GREEN_STONE = 4401;
	private static final int BLUE_STONE = 4402;
	private static final int RED_STONE = 4403;
	
	public ToIVortex()
	{
		super(-1, "teleports");
		
		addStartNpc(30952, 30953, 30954);
		addTalkId(30952, 30953, 30954);
		addFirstTalkId(30952, 30953, 30954);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		if (event.equalsIgnoreCase("blue"))
		{
			if (st.hasQuestItems(BLUE_STONE))
			{
				st.takeItems(BLUE_STONE, 1);
				player.teleToLocation(114097, 19935, 935, 0);
			}
			else
				htmltext = "no-items.htm";
		}
		else if (event.equalsIgnoreCase("green"))
		{
			if (st.hasQuestItems(GREEN_STONE))
			{
				st.takeItems(GREEN_STONE, 1);
				player.teleToLocation(110930, 15963, -4378, 0);
			}
			else
				htmltext = "no-items.htm";
		}
		else if (event.equalsIgnoreCase("red"))
		{
			if (st.hasQuestItems(RED_STONE))
			{
				st.takeItems(RED_STONE, 1);
				player.teleToLocation(118558, 16659, 5987, 0);
			}
			else
				htmltext = "no-items.htm";
		}
		st.exitQuest(true);
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
		return npc.getNpcId() + ".htm";
	}
}