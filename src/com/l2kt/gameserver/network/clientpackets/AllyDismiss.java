package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.gameserver.data.sql.ClanTable;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.pledge.Clan;
import com.l2kt.gameserver.network.SystemMessageId;

public final class AllyDismiss extends L2GameClientPacket
{
	private String _clanName;
	
	@Override
	protected void readImpl()
	{
		_clanName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (_clanName == null)
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final Clan leaderClan = player.getClan();
		if (leaderClan == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}
		
		if (leaderClan.getAllyId() == 0)
		{
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		
		if (!player.isClanLeader() || leaderClan.getClanId() != leaderClan.getAllyId())
		{
			player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		
		Clan clan = ClanTable.getInstance().getClanByName(_clanName);
		if (clan == null)
		{
			player.sendPacket(SystemMessageId.CLAN_DOESNT_EXISTS);
			return;
		}
		
		if (clan.getClanId() == leaderClan.getClanId())
		{
			player.sendPacket(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW);
			return;
		}
		
		if (clan.getAllyId() != leaderClan.getAllyId())
		{
			player.sendPacket(SystemMessageId.DIFFERENT_ALLIANCE);
			return;
		}
		
		long currentTime = System.currentTimeMillis();
		leaderClan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED * 86400000L, Clan.PENALTY_TYPE_DISMISS_CLAN);
		leaderClan.updateClanInDB();
		
		clan.setAllyId(0);
		clan.setAllyName(null);
		clan.changeAllyCrest(0, true);
		clan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED * 86400000L, Clan.PENALTY_TYPE_CLAN_DISMISSED);
		clan.updateClanInDB();
		
		player.sendPacket(SystemMessageId.YOU_HAVE_EXPELED_A_CLAN);
	}
}