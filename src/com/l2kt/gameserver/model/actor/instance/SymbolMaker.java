package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.gameserver.data.xml.HennaData;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.HennaEquipList;
import com.l2kt.gameserver.network.serverpackets.HennaRemoveList;

public class SymbolMaker extends Folk
{
	public SymbolMaker(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.equals("Draw"))
			player.sendPacket(new HennaEquipList(player, HennaData.INSTANCE.getAvailableHennasFor(player)));
		else if (command.equals("RemoveList"))
		{
			boolean hasHennas = false;
			for (int i = 1; i <= 3; i++)
			{
				if (player.getHenna(i) != null)
					hasHennas = true;
			}
			
			if (hasHennas)
				player.sendPacket(new HennaRemoveList(player));
			else
				player.sendPacket(SystemMessageId.SYMBOL_NOT_FOUND);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/symbolmaker/SymbolMaker.htm";
	}
}