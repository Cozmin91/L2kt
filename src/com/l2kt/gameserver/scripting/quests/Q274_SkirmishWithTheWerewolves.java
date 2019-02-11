package com.l2kt.gameserver.scripting.quests;

import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.base.ClassRace;
import com.l2kt.gameserver.scripting.Quest;
import com.l2kt.gameserver.scripting.QuestState;

public class Q274_SkirmishWithTheWerewolves extends Quest
{
	private static final String qn = "Q274_SkirmishWithTheWerewolves";
	
	// Needed items
	private static final int NECKLACE_OF_VALOR = 1507;
	private static final int NECKLACE_OF_COURAGE = 1506;
	
	// Items
	private static final int MARAKU_WEREWOLF_HEAD = 1477;
	private static final int MARAKU_WOLFMEN_TOTEM = 1501;
	
	public Q274_SkirmishWithTheWerewolves()
	{
		super(274, "Skirmish with the Werewolves");
		
		setItemsIds(MARAKU_WEREWOLF_HEAD, MARAKU_WOLFMEN_TOTEM);
		
		addStartNpc(30569);
		addTalkId(30569);
		
		addKillId(20363, 20364);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = event;
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30569-03.htm"))
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
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getRace() != ClassRace.ORC)
					htmltext = "30569-00.htm";
				else if (player.getLevel() < 9)
					htmltext = "30569-01.htm";
				else if (st.hasAtLeastOneQuestItem(NECKLACE_OF_COURAGE, NECKLACE_OF_VALOR))
					htmltext = "30569-02.htm";
				else
					htmltext = "30569-07.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "30569-04.htm";
				else
				{
					htmltext = "30569-05.htm";
					
					int amount = 3500 + st.getQuestItemsCount(MARAKU_WOLFMEN_TOTEM) * 600;
					
					st.takeItems(MARAKU_WEREWOLF_HEAD, -1);
					st.takeItems(MARAKU_WOLFMEN_TOTEM, -1);
					st.rewardItems(57, amount);
					
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
		
		if (st.dropItemsAlways(MARAKU_WEREWOLF_HEAD, 1, 40))
			st.set("cond", "2");
		
		if (Rnd.INSTANCE.get(100) < 6)
			st.giveItems(MARAKU_WOLFMEN_TOTEM, 1);
		
		return null;
	}
}