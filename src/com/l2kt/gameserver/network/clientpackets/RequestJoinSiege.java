package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.manager.CastleManager;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.entity.Castle;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SiegeInfo;

public final class RequestJoinSiege extends L2GameClientPacket
{
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null)
			return;
		
		if (_isJoining == 1)
		{
			if (System.currentTimeMillis() < player.getClan().getDissolvingExpiryTime())
			{
				player.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
				return;
			}
			
			if (_isAttacker == 1)
				castle.getSiege().registerAttacker(player);
			else
				castle.getSiege().registerDefender(player);
		}
		else
			castle.getSiege().unregisterClan(player.getClan());
		
		player.sendPacket(new SiegeInfo(castle));
	}
}