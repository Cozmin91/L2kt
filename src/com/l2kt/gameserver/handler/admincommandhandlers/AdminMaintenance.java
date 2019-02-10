package com.l2kt.gameserver.handler.admincommandhandlers;

import com.l2kt.Config;
import com.l2kt.commons.util.SysUtil;
import com.l2kt.gameserver.LoginServerThread;
import com.l2kt.gameserver.Shutdown;
import com.l2kt.gameserver.handler.IAdminCommandHandler;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;

import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager;
import com.l2kt.loginserver.network.gameserverpackets.ServerStatus;

public class AdminMaintenance implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_server",
		
		"admin_server_shutdown",
		"admin_server_restart",
		"admin_server_abort",
		
		"admin_server_gm_only",
		"admin_server_all",
		"admin_server_max_player",
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_server"))
			sendHtmlForm(activeChar);
		else if (command.startsWith("admin_server_shutdown"))
		{
			try
			{
				Shutdown.Companion.getInstance().startShutdown(activeChar, null, Integer.parseInt(command.substring(22)), false);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_restart"))
		{
			try
			{
				Shutdown.Companion.getInstance().startShutdown(activeChar, null, Integer.parseInt(command.substring(21)), true);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_abort"))
		{
			Shutdown.Companion.getInstance().abort(activeChar);
		}
		else if (command.equals("admin_server_gm_only"))
		{
			LoginServerThread.INSTANCE.setServerStatus(ServerStatus.STATUS_GM_ONLY);
			Config.SERVER_GMONLY = true;
			
			activeChar.sendMessage("Server is now setted as GMonly.");
			sendHtmlForm(activeChar);
		}
		else if (command.equals("admin_server_all"))
		{
			LoginServerThread.INSTANCE.setServerStatus(ServerStatus.STATUS_AUTO);
			Config.SERVER_GMONLY = false;
			
			activeChar.sendMessage("Server isn't setted as GMonly anymore.");
			sendHtmlForm(activeChar);
		}
		else if (command.startsWith("admin_server_max_player"))
		{
			try
			{
				final int number = Integer.parseInt(command.substring(24));
				
				LoginServerThread.INSTANCE.setMaxPlayer(number);
				activeChar.sendMessage("Server maximum player amount is setted to " + number + ".");
				sendHtmlForm(activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("The parameter must be a valid number.");
			}
		}
		return true;
	}
	
	private static void sendHtmlForm(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/maintenance.htm");
		html.replace("%count%", World.getInstance().getPlayers().size());
		html.replace("%used%", SysUtil.getUsedMemory());
		html.replace("%server_name%", LoginServerThread.INSTANCE.getServerName());
		html.replace("%status%", LoginServerThread.INSTANCE.getStatusString());
		html.replace("%max_players%", LoginServerThread.INSTANCE.getMaxPlayers());
		html.replace("%time%", GameTimeTaskManager.getInstance().getGameTimeFormated());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}