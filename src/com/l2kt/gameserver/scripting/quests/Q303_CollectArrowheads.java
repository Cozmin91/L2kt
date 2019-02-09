package com.l2kt.gameserver.scripting.quests;

import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.scripting.Quest;
import com.l2kt.gameserver.scripting.QuestState;

public class Q303_CollectArrowheads extends Quest
{
	private static final String qn = "Q303_CollectArrowheads";
	
	// Item
	private static final int ORCISH_ARROWHEAD = 963;
	
	public Q303_CollectArrowheads()
	{
		super(303, "Collect Arrowheads");
		
		setItemsIds(ORCISH_ARROWHEAD);
		
		addStartNpc(30029); // Minia
		addTalkId(30029);
		
		addKillId(20361);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30029-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = (player.getLevel() < 10) ? "30029-01.htm" : "30029-02.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "30029-04.htm";
				else
				{
					htmltext = "30029-05.htm";
					st.takeItems(ORCISH_ARROWHEAD, -1);
					st.rewardItems(57, 1000);
					st.rewardExpAndSp(2000, 0);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(ORCISH_ARROWHEAD, 1, 10, 400000))
			st.set("cond", "2");
		
		return null;
	}
}