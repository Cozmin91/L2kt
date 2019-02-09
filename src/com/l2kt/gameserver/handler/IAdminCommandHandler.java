package com.l2kt.gameserver.handler;

import com.l2kt.gameserver.model.actor.instance.Player;

public interface IAdminCommandHandler
{
	/**
	 * this is the worker method that is called when someone uses an admin command.
	 * @param activeChar
	 * @param command
	 * @return command success
	 */
	public boolean useAdminCommand(String command, Player activeChar);
	
	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public String[] getAdminCommandList();
}
