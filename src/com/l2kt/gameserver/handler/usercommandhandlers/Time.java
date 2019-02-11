package com.l2kt.gameserver.handler.usercommandhandlers;

import com.l2kt.gameserver.handler.IUserCommandHandler;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager;

public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		77
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		final int hour = GameTimeTaskManager.INSTANCE.getGameHour();
		final int minute = GameTimeTaskManager.INSTANCE.getGameMinute();
		
		final String min = ((minute < 10) ? "0" : "") + minute;
		
		activeChar.sendPacket(SystemMessage.Companion.getSystemMessage((GameTimeTaskManager.INSTANCE.isNight()) ? SystemMessageId.TIME_S1_S2_IN_THE_NIGHT : SystemMessageId.TIME_S1_S2_IN_THE_DAY).addNumber(hour).addString(min));
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}