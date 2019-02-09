package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.model.actor.instance.Player;

public class PrivateStoreMsgSell extends L2GameServerPacket
{
	private final Player _activeChar;
	private String _storeMsg;
	
	public PrivateStoreMsgSell(Player player)
	{
		_activeChar = player;
		if (_activeChar.getSellList() != null)
			_storeMsg = _activeChar.getSellList().getTitle();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9c);
		writeD(_activeChar.getObjectId());
		writeS(_storeMsg);
	}
}