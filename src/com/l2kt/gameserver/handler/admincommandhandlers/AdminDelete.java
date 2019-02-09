package com.l2kt.gameserver.handler.admincommandhandlers;

import com.l2kt.gameserver.data.SpawnTable;
import com.l2kt.gameserver.handler.IAdminCommandHandler;
import com.l2kt.gameserver.model.L2Spawn;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2kt.gameserver.network.SystemMessageId;

/**
 * This class handles following admin commands: - delete = deletes target
 */
public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_delete"))
			handleDelete(activeChar);
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void handleDelete(Player activeChar)
	{
		WorldObject obj = activeChar.getTarget();
		if (obj != null && obj instanceof Npc)
		{
			Npc target = (Npc) obj;
			
			L2Spawn spawn = target.getSpawn();
			if (spawn != null)
			{
				spawn.setRespawnState(false);
				
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				else
					SpawnTable.getInstance().deleteSpawn(spawn, true);
			}
			target.deleteMe();
			
			activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
		}
		else
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
	}
}