package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.model.actor.instance.Player;

/**
 * Format: ch Sddddddddd
 * @author KenM
 */
public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
	private final Player _activeChar;
	
	public ExDuelUpdateUserInfo(Player cha)
	{
		_activeChar = cha;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4f);
		writeS(_activeChar.getName());
		writeD(_activeChar.getObjectId());
		writeD(_activeChar.getClassId().getId());
		writeD(_activeChar.getLevel());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentMp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getCurrentCp());
		writeD(_activeChar.getMaxCp());
	}
}