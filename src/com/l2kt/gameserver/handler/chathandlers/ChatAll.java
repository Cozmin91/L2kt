package com.l2kt.gameserver.handler.chathandlers;

import com.l2kt.gameserver.handler.IChatHandler;
import com.l2kt.gameserver.model.BlockList;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.FloodProtectors;
import com.l2kt.gameserver.network.FloodProtectors.Action;
import com.l2kt.gameserver.network.serverpackets.CreatureSay;

public class ChatAll implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		0
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String params, String text)
	{
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.GLOBAL_CHAT))
			return;
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		for (Player player : activeChar.getKnownTypeInRadius(Player.class, 1250))
		{
			if (!BlockList.isBlocked(player, activeChar))
				player.sendPacket(cs);
		}
		activeChar.sendPacket(cs);
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}