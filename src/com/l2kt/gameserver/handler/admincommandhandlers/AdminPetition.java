package com.l2kt.gameserver.handler.admincommandhandlers;

import com.l2kt.gameserver.data.manager.PetitionManager;
import com.l2kt.gameserver.handler.IAdminCommandHandler;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles commands for GMs to respond to petitions.
 * @author Tempy
 */
public class AdminPetition implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_view_petitions",
		"admin_view_petition",
		"admin_accept_petition",
		"admin_reject_petition",
		"admin_reset_petitions"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		int petitionId = -1;
		
		try
		{
			petitionId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
		}
		
		if (command.equals("admin_view_petitions"))
			PetitionManager.INSTANCE.sendPendingPetitionList(activeChar);
		else if (command.startsWith("admin_view_petition"))
			PetitionManager.INSTANCE.viewPetition(activeChar, petitionId);
		else if (command.startsWith("admin_accept_petition"))
		{
			if (PetitionManager.INSTANCE.isPlayerInConsultation(activeChar))
			{
				activeChar.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME));
				return true;
			}
			
			if (PetitionManager.INSTANCE.isPetitionInProcess(petitionId))
			{
				activeChar.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
				return true;
			}
			
			if (!PetitionManager.INSTANCE.acceptPetition(activeChar, petitionId))
				activeChar.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION));
		}
		else if (command.startsWith("admin_reject_petition"))
		{
			if (!PetitionManager.INSTANCE.rejectPetition(activeChar, petitionId))
				activeChar.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER));
		}
		else if (command.equals("admin_reset_petitions"))
		{
			if (PetitionManager.INSTANCE.isPetitionInProcess())
			{
				activeChar.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
				return false;
			}
			PetitionManager.INSTANCE.getPendingPetitions().clear();
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}