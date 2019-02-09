package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author zabbix Lets drink to code!
 */
public final class RequestLinkHtml extends L2GameClientPacket
{
	private String _link;
	
	@Override
	protected void readImpl()
	{
		_link = readS();
	}
	
	@Override
	public void runImpl()
	{
		final Player actor = getClient().getActiveChar();
		if (actor == null)
			return;
		
		if (_link.contains("..") || !_link.contains(".htm"))
			return;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(_link);
		sendPacket(html);
	}
}