package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.gameserver.model.pledge.Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private final Clan _clan;
	
	public PledgeShowInfoUpdate(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x88);
		writeD(_clan.getClanId());
		writeD(_clan.getCrestId());
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		writeD(_clan.getHideoutId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0);
		writeD(0);
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName()); // c5
		writeD(_clan.getAllyCrestId()); // c5
		writeD(_clan.isAtWar() ? 1 : 0); // c5
	}
}