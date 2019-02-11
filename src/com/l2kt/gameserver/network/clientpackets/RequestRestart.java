package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.network.L2GameClient;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.instancemanager.SevenSignsFestival;
import com.l2kt.gameserver.network.serverpackets.CharSelectInfo;
import com.l2kt.gameserver.network.serverpackets.RestartResponse;
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestRestart extends L2GameClientPacket
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
		
		if (player.getActiveEnchantItem() != null || player.isLocked() || player.isInStoreMode())
		{
			sendPacket(RestartResponse.Companion.valueOf(false));
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_RESTART))
		{
			player.sendPacket(SystemMessageId.NO_RESTART_HERE);
			sendPacket(RestartResponse.Companion.valueOf(false));
			return;
		}
		
		if (AttackStanceTaskManager.INSTANCE.isInAttackStance(player))
		{
			player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
			sendPacket(RestartResponse.Companion.valueOf(false));
			return;
		}
		
		if (player.isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized())
		{
			player.sendPacket(SystemMessageId.NO_RESTART_HERE);
			sendPacket(RestartResponse.Companion.valueOf(false));
			return;
		}
		
		player.removeFromBossZone();
		
		final L2GameClient client = getClient();
		
		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);
		
		// removing player from the world
		player.deleteMe();
		
		client.setActiveChar(null);
		client.setState(L2GameClient.GameClientState.AUTHED);
		
		sendPacket(RestartResponse.Companion.valueOf(true));
		
		// send char list
		final CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().getPlayOkID1());
		sendPacket(cl);
		client.setCharSelectSlot(cl.getCharacterSlots());
	}
}