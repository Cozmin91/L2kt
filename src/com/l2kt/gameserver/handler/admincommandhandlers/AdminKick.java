package com.l2kt.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2kt.gameserver.handler.IAdminCommandHandler;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.instance.Player;

public class AdminKick implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_character_disconnect",
		"admin_kick",
		"admin_kick_non_gm"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_character_disconnect") || command.equals("admin_kick"))
			disconnectCharacter(activeChar);
		
		if (command.startsWith("admin_kick"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				Player plyr = World.INSTANCE.getPlayer(player);
				if (plyr != null)
				{
					plyr.logout(false);
					activeChar.sendMessage(plyr.getName() + " have been kicked from server.");
				}
			}
		}
		
		if (command.startsWith("admin_kick_non_gm"))
		{
			int counter = 0;
			
			for (Player player : World.INSTANCE.getPlayers())
			{
				if (player.isGM())
					continue;
				
				counter++;
				player.logout(false);
			}
			activeChar.sendMessage("A total of " + counter + " players have been kicked.");
		}
		return true;
	}
	
	private static void disconnectCharacter(Player activeChar)
	{
		WorldObject target = activeChar.getTarget();
		Player player = null;
		
		if (target instanceof Player)
			player = (Player) target;
		else
			return;
		
		if (player == activeChar)
			activeChar.sendMessage("You cannot disconnect your own character.");
		else
		{
			activeChar.sendMessage(player.getName() + " have been kicked from server.");
			player.logout(false);
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}