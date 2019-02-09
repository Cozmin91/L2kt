package com.l2kt.gameserver.handler.admincommandhandlers;

import com.l2kt.gameserver.handler.IAdminCommandHandler;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;

/**
 * This class handles following admin commands: - invul = turns invulnerability on/off
 */
public class AdminInvul implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_setinvul"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player player)
	{
		if (command.equals("admin_setinvul"))
		{
			WorldObject object = player.getTarget();
			if (object == null)
				object = player;
			
			if (!(object instanceof Creature))
			{
				player.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Creature target = (Creature) object;
			target.setIsMortal(!target.isMortal());
			
			player.sendMessage(target.getName() + ((!target.isMortal()) ? " is now invulnerable." : " is now mortal."));
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}