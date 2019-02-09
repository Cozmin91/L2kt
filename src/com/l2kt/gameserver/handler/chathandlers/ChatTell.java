package com.l2kt.gameserver.handler.chathandlers;

import com.l2kt.gameserver.handler.IChatHandler;
import com.l2kt.gameserver.model.BlockList;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.CreatureSay;

public class ChatTell implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		2
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		if (target == null)
			return;
		
		final Player receiver = World.getInstance().getPlayer(target);
		if (receiver == null || receiver.getClient().isDetached())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}
		
		if (activeChar.equals(receiver))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		if (receiver.isInJail() || receiver.isChatBanned())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_CHAT_BANNED);
			return;
		}
		
		if (!activeChar.isGM() && (receiver.isInRefusalMode() || BlockList.isBlocked(receiver, activeChar)))
		{
			activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			return;
		}
		
		receiver.sendPacket(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text));
		activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), type, "->" + receiver.getName(), text));
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}