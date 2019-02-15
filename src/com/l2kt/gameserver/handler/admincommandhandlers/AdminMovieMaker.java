package com.l2kt.gameserver.handler.admincommandhandlers;

import com.l2kt.gameserver.data.manager.MovieMakerManager;
import com.l2kt.gameserver.handler.IAdminCommandHandler;
import com.l2kt.gameserver.model.actor.instance.Player;

/**
 * @author KKnD
 */
public class AdminMovieMaker implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_addseq",
		"admin_playseqq",
		"admin_delsequence",
		"admin_editsequence",
		"admin_addsequence",
		"admin_playsequence",
		"admin_movie",
		"admin_updatesequence",
		"admin_broadcast",
		"admin_playmovie",
		"admin_broadmovie"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_movie"))
		{
			MovieMakerManager.INSTANCE.mainHtm(activeChar);
		}
		else if (command.startsWith("admin_playseqq"))
		{
			try
			{
				MovieMakerManager.INSTANCE.playSequence(Integer.parseInt(command.substring(15)), activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("You entered an invalid sequence id.");
				MovieMakerManager.INSTANCE.mainHtm(activeChar);
				return false;
			}
		}
		else if (command.equals("admin_addseq"))
		{
			MovieMakerManager.INSTANCE.addSequence(activeChar);
		}
		else if (command.startsWith("admin_delsequence"))
		{
			try
			{
				MovieMakerManager.INSTANCE.deleteSequence(Integer.parseInt(command.substring(18)), activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("You entered an invalid sequence id.");
				MovieMakerManager.INSTANCE.mainHtm(activeChar);
				return false;
			}
		}
		else if (command.startsWith("admin_broadcast"))
		{
			try
			{
				MovieMakerManager.INSTANCE.broadcastSequence(Integer.parseInt(command.substring(16)), activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("You entered an invalid sequence id.");
				MovieMakerManager.INSTANCE.mainHtm(activeChar);
				return false;
			}
		}
		else if (command.equals("admin_playmovie"))
		{
			MovieMakerManager.INSTANCE.playMovie(0, activeChar);
		}
		else if (command.equals("admin_broadmovie"))
		{
			MovieMakerManager.INSTANCE.playMovie(1, activeChar);
		}
		else if (command.startsWith("admin_editsequence"))
		{
			try
			{
				MovieMakerManager.INSTANCE.editSequence(Integer.parseInt(command.substring(19)), activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("You entered an invalid sequence id.");
				MovieMakerManager.INSTANCE.mainHtm(activeChar);
				return false;
			}
		}
		else
		{
			String[] args = command.split(" ");
			if (args.length < 10)
			{
				activeChar.sendMessage("Some arguments are missing.");
				return false;
			}
			
			final int targ = (activeChar.getTarget() != null) ? activeChar.getTarget().getObjectId() : activeChar.getObjectId();
			
			if (command.startsWith("admin_addsequence"))
			{
				MovieMakerManager.INSTANCE.addSequence(activeChar, Integer.parseInt(args[1]), targ, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9]));
			}
			else if (command.startsWith("admin_playsequence"))
			{
				MovieMakerManager.INSTANCE.playSequence(activeChar, targ, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]));
			}
			else if (command.startsWith("admin_updatesequence"))
			{
				MovieMakerManager.INSTANCE.updateSequence(activeChar, Integer.parseInt(args[1]), targ, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9]));
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}