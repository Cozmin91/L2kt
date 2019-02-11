package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.CharSelectSlot;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.FloodProtectors;
import com.l2kt.gameserver.network.L2GameClient;
import com.l2kt.gameserver.network.serverpackets.CharSelected;
import com.l2kt.gameserver.network.serverpackets.SSQInfo;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;
	
	@SuppressWarnings("unused")
	private int _unk1; // new in C4
	@SuppressWarnings("unused")
	private int _unk2; // new in C4
	@SuppressWarnings("unused")
	private int _unk3; // new in C4
	@SuppressWarnings("unused")
	private int _unk4; // new in C4
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		if (!FloodProtectors.INSTANCE.performAction(client, FloodProtectors.Action.CHARACTER_SELECT))
			return;
		
		// we should always be able to acquire the lock but if we cant lock then nothing should be done (ie repeated packet)
		if (client.getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null but if not then this is repeated packet and nothing should be done here
				if (client.getActiveChar() == null)
				{
					final CharSelectSlot info = client.getCharSelectSlot(_charSlot);
					if (info == null || info.getAccessLevel() < 0)
						return;
					
					// Load up character from disk
					final Player cha = client.loadCharFromDisk(_charSlot);
					if (cha == null)
						return;
					
					cha.setClient(client);
					client.setActiveChar(cha);
					cha.setOnlineStatus(true, true);
					
					sendPacket(SSQInfo.Companion.sendSky());
					
					client.setState(L2GameClient.GameClientState.IN_GAME);
					
					sendPacket(new CharSelected(cha, client.getSessionId().getPlayOkID1()));
				}
			}
			finally
			{
				client.getActiveCharLock().unlock();
			}
		}
	}
}