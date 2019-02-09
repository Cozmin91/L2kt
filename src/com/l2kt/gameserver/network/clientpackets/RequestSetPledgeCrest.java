package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.cache.CrestCache;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.pledge.Clan;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.idfactory.IdFactory;

public final class RequestSetPledgeCrest extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 256)
			return;
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		if (_length < 0 || _length > 256)
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final Clan clan = player.getClan();
		if (clan == null)
			return;
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS);
			return;
		}
		
		if ((player.getClanPrivileges() & Clan.CP_CL_REGISTER_CREST) != Clan.CP_CL_REGISTER_CREST)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (_length == 0 || _data.length == 0)
		{
			if (clan.getCrestId() != 0)
			{
				clan.changeClanCrest(0);
				player.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED);
			}
		}
		else
		{
			if (clan.getLevel() < 3)
			{
				player.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST);
				return;
			}
			
			final int crestId = IdFactory.getInstance().getNextId();
			if (CrestCache.getInstance().saveCrest(CrestCache.CrestType.PLEDGE, crestId, _data))
			{
				clan.changeClanCrest(crestId);
				player.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);
			}
		}
	}
}