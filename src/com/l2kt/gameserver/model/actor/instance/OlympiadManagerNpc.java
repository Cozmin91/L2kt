package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.commons.lang.StringUtil;
import com.l2kt.gameserver.data.xml.MultisellData;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.entity.Hero;
import com.l2kt.gameserver.model.olympiad.*;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.ExHeroList;
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OlympiadManagerNpc extends Folk
{
	private static final List<OlympiadManagerNpc> _managers = new CopyOnWriteArrayList<>();
	
	private static final int GATE_PASS = 6651;
	
	public OlympiadManagerNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public static List<OlympiadManagerNpc> getInstances()
	{
		return _managers;
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		// Only used by Olympiad managers. Monument of Heroes don't use "Chat" bypass.
		String filename = "noble";
		
		if (val > 0)
			filename = "noble_" + val;
		
		return filename + ".htm";
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		int npcId = ((NpcTemplate)getTemplate()).getNpcId();
		String filename = getHtmlPath(npcId, val);
		
		switch (npcId)
		{
			case 31688: // Olympiad managers
				if (player.isNoble() && val == 0)
					filename = "noble_main.htm";
				break;
			
			case 31690: // Monuments of Heroes
			case 31769:
			case 31770:
			case 31771:
			case 31772:
				if (player.isHero() || Hero.INSTANCE.isInactiveHero(player.getObjectId()))
					filename = "hero_main.htm";
				else
					filename = "hero_main2.htm";
				break;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/olympiad/" + filename);
		
		// Hidden option for players who are in inactive mode.
		if (filename == "hero_main.htm")
		{
			String hiddenText = "";
			if (Hero.INSTANCE.isInactiveHero(player.getObjectId()))
				hiddenText = "<a action=\"bypass -h npc_%objectId%_Olympiad 5\">\"I want to be a Hero.\"</a><br>";
			
			html.replace("%hero%", hiddenText);
		}
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("OlympiadNoble"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			if (player.isCursedWeaponEquipped())
			{
				html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_cant_cw.htm");
				player.sendPacket(html);
				return;
			}
			
			if (player.getClassIndex() != 0)
			{
				html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_cant_sub.htm");
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
				return;
			}
			
			if (!player.isNoble() || (player.getClassId().level() < 3))
			{
				html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_cant_thirdclass.htm");
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
				return;
			}
			
			int val = Integer.parseInt(command.substring(14));
			switch (val)
			{
				case 1: // Unregister
					OlympiadManager.INSTANCE.unRegisterNoble(player);
					break;
				
				case 2: // Show waiting list
					final int nonClassed = OlympiadManager.INSTANCE.getRegisteredNonClassBased().size();
					final int classed = OlympiadManager.INSTANCE.getRegisteredClassBased().size();
					
					html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_registered.htm");
					html.replace("%listClassed%", classed);
					html.replace("%listNonClassed%", nonClassed);
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
					break;
				
				case 3: // There are %points% Grand Olympiad points granted for this event.
					int points = Olympiad.INSTANCE.getNoblePoints(player.getObjectId());
					html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_points1.htm");
					html.replace("%points%", points);
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
					break;
				
				case 4: // register non classed based
					OlympiadManager.INSTANCE.registerNoble(player, CompetitionType.NON_CLASSED);
					break;
				
				case 5: // register classed based
					OlympiadManager.INSTANCE.registerNoble(player, CompetitionType.CLASSED);
					break;
				
				case 6: // request tokens reward
					html.setFile(Olympiad.OLYMPIAD_HTML_PATH + ((Olympiad.INSTANCE.getNoblessePasses(player, false) > 0) ? "noble_settle.htm" : "noble_nopoints2.htm"));
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
					break;
				
				case 7: // Rewards
					MultisellData.INSTANCE.separateAndSend("102", player, this, false);
					break;
				
				case 10: // Give tokens to player
					player.addItem("Olympiad", GATE_PASS, Olympiad.INSTANCE.getNoblessePasses(player, true), player, true);
					break;
				
				default:
					break;
			}
		}
		else if (command.startsWith("Olympiad"))
		{
			int val = Integer.parseInt(command.substring(9, 10));
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			switch (val)
			{
				case 2: // Show rank for a specific class, example >> Olympiad 1_88
					int classId = Integer.parseInt(command.substring(11));
					if (classId >= 88 && classId <= 118)
					{
						List<String> names = Olympiad.INSTANCE.getClassLeaderBoard(classId);
						html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_ranking.htm");
						
						int index = 1;
						for (String name : names)
						{
							html.replace("%place" + index + "%", index);
							html.replace("%rank" + index + "%", name);
							
							index++;
							if (index > 10)
								break;
						}
						
						for (; index <= 10; index++)
						{
							html.replace("%place" + index + "%", "");
							html.replace("%rank" + index + "%", "");
						}
						
						html.replace("%objectId%", getObjectId());
						player.sendPacket(html);
					}
					break;
				
				case 3: // Spectator overview
					html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_observe_list.htm");
					
					int i = 0;
					
					final StringBuilder sb = new StringBuilder(2000);
					for (OlympiadGameTask task : OlympiadGameManager.INSTANCE.getOlympiadTasks())
					{
						StringUtil.INSTANCE.append(sb, "<a action=\"bypass arenachange ", i, "\">Arena ", ++i, "&nbsp;");
						
						if (task.isGameStarted())
						{
							if (task.isInTimerTime())
								StringUtil.INSTANCE.append(sb, "(&$907;)"); // Counting In Progress
							else if (task.isBattleStarted())
								StringUtil.INSTANCE.append(sb, "(&$829;)"); // In Progress
							else
								StringUtil.INSTANCE.append(sb, "(&$908;)"); // Terminate
								
							StringUtil.INSTANCE.append(sb, "&nbsp;", task.getGame().getPlayerNames()[0], "&nbsp; : &nbsp;", task.getGame().getPlayerNames()[1]);
						}
						else
							StringUtil.INSTANCE.append(sb, "(&$906;)</td><td>&nbsp;"); // Initial State
							
						StringUtil.INSTANCE.append(sb, "</a><br>");
					}
					html.replace("%list%", sb.toString());
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
					break;
				
				case 4: // Send heroes list.
					player.sendPacket(new ExHeroList());
					break;
				
				case 5: // Hero pending state.
					if (Hero.INSTANCE.isInactiveHero(player.getObjectId()))
					{
						html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "hero_confirm.htm");
						html.replace("%objectId%", getObjectId());
						player.sendPacket(html);
					}
					break;
				
				case 6: // Hero confirm action.
					if (Hero.INSTANCE.isInactiveHero(player.getObjectId()))
					{
						if (player.isSubClassActive() || player.getLevel() < 76)
						{
							player.sendMessage("You may only become an hero on a main class whose level is 75 or more.");
							return;
						}
						
						Hero.INSTANCE.activateHero(player);
					}
					break;
				
				case 7: // Main panel
					html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm");
					
					String hiddenText = "";
					if (Hero.INSTANCE.isInactiveHero(player.getObjectId()))
						hiddenText = "<a action=\"bypass -h npc_%objectId%_Olympiad 5\">\"I want to be a Hero.\"</a><br>";
					
					html.replace("%hero%", hiddenText);
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
					break;
				
				default:
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (getNpcId() == 31688)
			_managers.add(this);
	}
	
	@Override
	public void onDecay()
	{
		_managers.remove(this);
		super.onDecay();
	}
}