package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.data.manager.CursedWeaponManager;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		player.sendPacket(new ExCursedWeaponList(CursedWeaponManager.getInstance().getCursedWeaponsIds()));
	}
}