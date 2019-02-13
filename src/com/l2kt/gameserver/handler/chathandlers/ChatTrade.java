package com.l2kt.gameserver.handler.chathandlers;

import com.l2kt.gameserver.data.xml.MapRegionData;
import com.l2kt.gameserver.handler.IChatHandler;
import com.l2kt.gameserver.model.BlockList;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.FloodProtectors;
import com.l2kt.gameserver.network.FloodProtectors.Action;
import com.l2kt.gameserver.network.serverpackets.CreatureSay;

public class ChatTrade implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		8
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		if (!FloodProtectors.INSTANCE.performAction(activeChar.getClient(), Action.TRADE_CHAT))
			return;
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		final int region = MapRegionData.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
		
		for (Player player : World.getInstance().getPlayers())
		{
			if (!BlockList.Companion.isBlocked(player, activeChar) && region == MapRegionData.getInstance().getMapRegion(player.getX(), player.getY()))
				player.sendPacket(cs);
		}
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}