package com.l2kt.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2kt.gameserver.data.manager.CastleManager;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ItemList;

/**
 * An instance type extending {@link Folk}, used by Broadcasting Towers.<br>
 * <br>
 * Those NPCs allow {@link Player}s to spectate areas (sieges, olympiads).
 */
public final class BroadcastingTower extends Folk
{
	public BroadcastingTower(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("observe"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			final int cost = Integer.parseInt(st.nextToken());
			final int x = Integer.parseInt(st.nextToken());
			final int y = Integer.parseInt(st.nextToken());
			final int z = Integer.parseInt(st.nextToken());
			
			if (command.startsWith("observeSiege") && CastleManager.INSTANCE.getActiveSiege(x, y, z) == null)
			{
				player.sendPacket(SystemMessageId.ONLY_VIEW_SIEGE);
				return;
			}
			
			if (player.reduceAdena("Broadcast", cost, this, true))
			{
				player.enterObserverMode(x, y, z);
				player.sendPacket(new ItemList(player, false));
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/observation/" + filename + ".htm";
	}
}