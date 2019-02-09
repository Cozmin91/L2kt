package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.model.actor.instance.Player;

public class PrivateStoreMsgBuy extends L2GameServerPacket
{
	private final Player _activeChar;
	private String _storeMsg;
	
	public PrivateStoreMsgBuy(Player player)
	{
		_activeChar = player;
		if (_activeChar.getBuyList() != null)
			_storeMsg = _activeChar.getBuyList().getTitle();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb9);
		writeD(_activeChar.getObjectId());
		writeS(_storeMsg);
	}
}