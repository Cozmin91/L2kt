package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.Config;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.pledge.Clan;
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;

public class CastleBlacksmith extends Folk
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public CastleBlacksmith(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (!Config.ALLOW_MANOR)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/npcdefault.htm");
			html.replace("%objectId%", getObjectId());
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}
		
		if (validateCondition(player) != COND_OWNER)
			return;
		
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			showChatWindow(player, val);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		if (!Config.ALLOW_MANOR)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/npcdefault.htm");
			html.replace("%objectId%", getObjectId());
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}
		
		String filename = "data/html/castleblacksmith/castleblacksmith-no.htm";
		
		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
				filename = "data/html/castleblacksmith/castleblacksmith-busy.htm"; // Busy because of siege
			else if (condition == COND_OWNER) // Clan owns castle
			{
				if (val == 0)
					filename = "data/html/castleblacksmith/castleblacksmith.htm";
				else
					filename = "data/html/castleblacksmith/castleblacksmith-" + val + ".htm";
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		html.replace("%npcname%", getName());
		html.replace("%castleid%", getCastle().getCastleId());
		player.sendPacket(html);
	}
	
	protected int validateCondition(Player player)
	{
		if (getCastle() != null && player.getClan() != null)
		{
			if (getCastle().getSiege().isInProgress())
				return COND_BUSY_BECAUSE_OF_SIEGE;
			
			if (getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & Clan.CP_CS_MANOR_ADMIN) == Clan.CP_CS_MANOR_ADMIN)
				return COND_OWNER;
		}
		return COND_ALL_FALSE;
	}
}