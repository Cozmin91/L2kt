package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.Henna;

public class HennaRemoveList extends L2GameServerPacket
{
	private final Player _player;
	
	public HennaRemoveList(Player player)
	{
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe5);
		writeD(_player.getAdena());
		writeD(_player.getHennaEmptySlots());
		writeD(Math.abs(_player.getHennaEmptySlots() - 3));
		
		for (int i = 1; i <= 3; i++)
		{
			Henna henna = _player.getHenna(i);
			if (henna != null)
			{
				writeD(henna.getSymbolId());
				writeD(henna.getDyeId());
				writeD(Henna.getRequiredDyeAmount() / 2);
				writeD(henna.getPrice() / 5);
				writeD(0x01);
			}
		}
	}
}