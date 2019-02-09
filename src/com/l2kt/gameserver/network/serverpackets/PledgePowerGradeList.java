package com.l2kt.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.Set;

import com.l2kt.gameserver.model.pledge.ClanMember;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private final Set<Integer> _ranks;
	private final Collection<ClanMember> _members;
	
	public PledgePowerGradeList(Set<Integer> ranks, Collection<ClanMember> members)
	{
		_ranks = ranks;
		_members = members;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x3b);
		writeD(_ranks.size());
		for (int rank : _ranks)
		{
			writeD(rank);
			writeD((int) _members.stream().filter(m -> m.getPowerGrade() == rank).count());
		}
	}
}