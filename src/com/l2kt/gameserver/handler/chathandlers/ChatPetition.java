package com.l2kt.gameserver.handler.chathandlers;

import com.l2kt.gameserver.data.manager.PetitionManager;
import com.l2kt.gameserver.handler.IChatHandler;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;

public class ChatPetition implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		6,
		7
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT);
			return;
		}
		
		PetitionManager.getInstance().sendActivePetitionMessage(activeChar, text);
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}