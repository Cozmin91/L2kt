package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2kt.gameserver.network.serverpackets.SiegeInfo;

public class SiegeNpc extends Folk
{
	public SiegeNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		if (!getCastle().getSiege().isInProgress())
			player.sendPacket(new SiegeInfo(getCastle()));
		else
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/siege/" + getNpcId() + "-busy.htm");
			html.replace("%castlename%", getCastle().getName());
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
			player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
		}
	}
}