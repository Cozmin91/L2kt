package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.templates.skills.L2EffectFlag;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private final Player _player;
	
	public EtcStatusUpdate(Player player)
	{
		_player = player;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xF3);
		writeD(_player.getCharges());
		writeD(_player.getWeightPenalty());
		writeD((_player.isInRefusalMode() || _player.isChatBanned()) ? 1 : 0);
		writeD(_player.isInsideZone(ZoneId.DANGER_AREA) ? 1 : 0);
		writeD((_player.getExpertiseWeaponPenalty() || _player.getExpertiseArmorPenalty() > 0) ? 1 : 0);
		writeD(_player.isAffected(L2EffectFlag.CHARM_OF_COURAGE) ? 1 : 0);
		writeD(_player.getDeathPenaltyBuffLevel());
	}
}