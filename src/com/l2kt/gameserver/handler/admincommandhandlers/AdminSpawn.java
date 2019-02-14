package com.l2kt.gameserver.handler.admincommandhandlers;

import com.l2kt.commons.lang.StringUtil;
import com.l2kt.gameserver.data.SpawnTable;
import com.l2kt.gameserver.data.manager.FenceManager;
import com.l2kt.gameserver.data.xml.AdminData;
import com.l2kt.gameserver.data.xml.NpcData;
import com.l2kt.gameserver.extensions.BroadcastExtensionsKt;
import com.l2kt.gameserver.handler.IAdminCommandHandler;
import com.l2kt.gameserver.instancemanager.DayNightSpawnManager;
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2kt.gameserver.instancemanager.SevenSigns;
import com.l2kt.gameserver.model.L2Spawn;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Fence;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles following admin commands:<br>
 * - show_spawns = shows menu<br>
 * - spawn_index lvl = shows menu for monsters with respective level<br>
 * - spawn id = spawns monster id on target
 */
public class AdminSpawn implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_list_spawns",
		"admin_show_spawns",
		"admin_spawn",
		"admin_spawn_index",
		"admin_unspawnall",
		"admin_respawnall",
		"admin_spawn_reload",
		"admin_npc_index",
		"admin_spawn_once",
		"admin_show_npcs",
		"admin_spawnnight",
		"admin_spawnday",
		"admin_spawnfence",
		"admin_deletefence",
		"admin_listfence"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_list_spawns"))
		{
			int npcId = 0;
			
			try
			{
				String[] params = command.split(" ");
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher regexp = pattern.matcher(params[1]);
				
				if (regexp.matches())
					npcId = Integer.parseInt(params[1]);
				else
				{
					params[1] = params[1].replace('_', ' ');
					npcId = NpcData.INSTANCE.getTemplateByName(params[1]).getNpcId();
				}
			}
			catch (Exception e)
			{
				// If the parameter wasn't ok, then take the current target.
				final WorldObject target = activeChar.getTarget();
				if (target instanceof Npc)
					npcId = ((Npc) target).getNpcId();
			}
			
			// Load static Htm.
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/admin/listspawns.htm");
			
			// Generate data.
			final StringBuilder sb = new StringBuilder();
			
			int index = 0, x, y, z;
			String name = "";
			
			for (L2Spawn spawn : SpawnTable.INSTANCE.getSpawnTable())
			{
				if (npcId == spawn.getNpcId())
				{
					index++;
					name = spawn.getTemplate().getName();
					
					final Npc _npc = spawn.getNpc();
					if (_npc != null)
					{
						x = _npc.getX();
						y = _npc.getY();
						z = _npc.getZ();
					}
					else
					{
						x = spawn.getLocX();
						y = spawn.getLocY();
						z = spawn.getLocZ();
					}
					StringUtil.INSTANCE.append(sb, "<tr><td><a action=\"bypass -h admin_move_to ", x, " ", y, " ", z, "\">", index, " - (", x, " ", y, " ", z, ")", "</a></td></tr>");
				}
			}
			
			if (index == 0)
			{
				html.replace("%npcid%", "?");
				html.replace("%list%", "<tr><td>The parameter you entered as npcId is invalid.</td></tr>");
			}
			else
			{
				html.replace("%npcid%", name + " (" + npcId + ")");
				html.replace("%list%", sb.toString());
			}
			
			activeChar.sendPacket(html);
		}
		else if (command.equals("admin_show_spawns"))
			AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
		else if (command.startsWith("admin_spawn_index"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				int level = Integer.parseInt(st.nextToken());
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (NoSuchElementException nsee)
				{
				}
				showMonsters(activeChar, level, from);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		else if (command.equals("admin_show_npcs"))
			AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
		else if (command.startsWith("admin_npc_index"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				String letter = st.nextToken();
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (NoSuchElementException nsee)
				{
				}
				showNpcs(activeChar, letter, from);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
			}
		}
		else if (command.startsWith("admin_unspawnall"))
		{
			BroadcastExtensionsKt.toAllOnlinePlayers(SystemMessage.Companion.getSystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING));
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			World.getInstance().deleteVisibleNpcSpawns();
			AdminData.INSTANCE.broadcastMessageToGMs("NPCs' unspawn is now complete.");
		}
		else if (command.startsWith("admin_spawnday"))
			DayNightSpawnManager.getInstance().spawnDayCreatures();
		else if (command.startsWith("admin_spawnnight"))
			DayNightSpawnManager.getInstance().spawnNightCreatures();
		else if (command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload"))
		{
			// make sure all spawns are deleted
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			World.getInstance().deleteVisibleNpcSpawns();
			// now respawn all
			NpcData.INSTANCE.reload();
			SpawnTable.INSTANCE.reloadAll();
			RaidBossSpawnManager.getInstance().reloadBosses();
			SevenSigns.getInstance().spawnSevenSignsNPC();
			AdminData.INSTANCE.broadcastMessageToGMs("NPCs' respawn is now complete.");
		}
		else if (command.startsWith("admin_spawnfence"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				int type = Integer.parseInt(st.nextToken());
				int sizeX = (Integer.parseInt(st.nextToken()) / 100) * 100;
				int sizeY = (Integer.parseInt(st.nextToken()) / 100) * 100;
				int height = 1;
				if (st.hasMoreTokens())
					height = Math.min(Integer.parseInt(st.nextToken()), 3);
				
				FenceManager.getInstance().addFence(activeChar.getX(), activeChar.getY(), activeChar.getZ(), type, sizeX, sizeY, height);
				
				listFences(activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //spawnfence <type> <width> <length> [height]");
			}
		}
		else if (command.startsWith("admin_deletefence"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			try
			{
				WorldObject object = World.getInstance().getObject(Integer.parseInt(st.nextToken()));
				if (object instanceof Fence)
				{
					FenceManager.getInstance().removeFence((Fence) object);
					
					if (st.hasMoreTokens())
						listFences(activeChar);
				}
				else
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //deletefence <objectId>");
			}
		}
		else if (command.startsWith("admin_listfence"))
			listFences(activeChar);
		else if (command.startsWith("admin_spawn"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				String cmd = st.nextToken();
				String id = st.nextToken();
				int respawnTime = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 60;
				
				if (cmd.equalsIgnoreCase("admin_spawn_once"))
					spawn(activeChar, id, respawnTime, false);
				else
					spawn(activeChar, id, respawnTime, true);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void spawn(Player activeChar, String monsterId, int respawnTime, boolean permanent)
	{
		WorldObject target = activeChar.getTarget();
		if (target == null)
			target = activeChar;
		
		NpcTemplate template;
		
		if (monsterId.matches("[0-9]*")) // First parameter was an ID number
			template = NpcData.INSTANCE.getTemplate(Integer.parseInt(monsterId));
		else
		// First parameter wasn't just numbers, so go by name not ID
		{
			monsterId = monsterId.replace('_', ' ');
			template = NpcData.INSTANCE.getTemplateByName(monsterId);
		}
		
		try
		{
			L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(target.getX(), target.getY(), target.getZ(), activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			
			if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcId()) != null)
			{
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
				{
					activeChar.sendMessage("You cannot spawn another instance of " + template.getName() + ".");
					return;
				}
				
				spawn.setRespawnMinDelay(43200);
				spawn.setRespawnMaxDelay(129600);
				RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, 0, 0, permanent);
			}
			else
			{
				SpawnTable.INSTANCE.addNewSpawn(spawn, permanent);
				spawn.doSpawn(false);
				if (permanent)
					spawn.setRespawnState(true);
			}
			
			if (!permanent)
				spawn.setRespawnState(false);
			
			activeChar.sendMessage("Spawned " + template.getName() + ".");
			
		}
		catch (Exception e)
		{
			activeChar.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT);
		}
	}
	
	private static void showMonsters(Player activeChar, int level, int from)
	{
		final List<NpcTemplate> mobs = NpcData.INSTANCE.getTemplates(t -> t.isType("Monster") && t.getLevel() == level);
		final StringBuilder sb = new StringBuilder(200 + mobs.size() * 100);
		
		StringUtil.INSTANCE.append(sb, "<html><title>Spawn Monster:</title><body><p> Level : ", level, "<br>Total Npc's : ", mobs.size(), "<br>");
		
		int i = from;
		for (int j = 0; i < mobs.size() && j < 50; i++, j++)
			StringUtil.INSTANCE.append(sb, "<a action=\"bypass -h admin_spawn ", mobs.get(i).getNpcId(), "\">", mobs.get(i).getName(), "</a><br1>");
		
		if (i == mobs.size())
			sb.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
		else
			StringUtil.INSTANCE.append(sb, "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index ", level, " ", i, "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void showNpcs(Player activeChar, String starting, int from)
	{
		final List<NpcTemplate> mobs = NpcData.INSTANCE.getTemplates(t -> t.isType("Folk") && t.getName().startsWith(starting));
		final StringBuilder sb = new StringBuilder(200 + mobs.size() * 100);
		
		StringUtil.INSTANCE.append(sb, "<html><title>Spawn Monster:</title><body><p> There are ", mobs.size(), " Npcs whose name starts with ", starting, ":<br>");
		
		int i = from;
		for (int j = 0; i < mobs.size() && j < 50; i++, j++)
			StringUtil.INSTANCE.append(sb, "<a action=\"bypass -h admin_spawn ", mobs.get(i).getNpcId(), "\">", mobs.get(i).getName(), "</a><br1>");
		
		if (i == mobs.size())
			sb.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
		else
			StringUtil.INSTANCE.append(sb, "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index ", starting, " ", i, "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void listFences(Player activeChar)
	{
		final List<Fence> fences = FenceManager.getInstance().getFences();
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body>Total Fences: " + fences.size() + "<br><br>");
		for (Fence fence : fences)
			sb.append("<a action=\"bypass -h admin_deletefence " + fence.getObjectId() + " 1\">Fence: " + fence.getObjectId() + " [" + fence.getX() + " " + fence.getY() + " " + fence.getZ() + "]</a><br>");
		sb.append("</body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
}