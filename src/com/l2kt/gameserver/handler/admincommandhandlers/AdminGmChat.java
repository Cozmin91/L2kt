package com.l2kt.gameserver.handler.admincommandhandlers;

import com.l2kt.gameserver.data.xml.AdminData;
import com.l2kt.gameserver.handler.IAdminCommandHandler;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.clientpackets.Say2;
import com.l2kt.gameserver.network.serverpackets.CreatureSay;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>gmchat : sends text to all online GM's</li>
 * <li>gmchat_menu : same as gmchat, but displays the admin panel after chat</li>
 * </ul>
 */
public class AdminGmChat implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gmchat",
		"admin_gmchat_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_gmchat"))
		{
			try
			{
				AdminData.INSTANCE.broadcastToGMs(new CreatureSay(0, Say2.ALLIANCE, activeChar.getName(), command.substring((command.startsWith("admin_gmchat_menu")) ? 18 : 13)));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// empty message.. ignore
			}
			
			if (command.startsWith("admin_gmchat_menu"))
				AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}