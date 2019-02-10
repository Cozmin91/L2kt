package com.l2kt.loginserver.network.loginserverpackets;

import com.l2kt.loginserver.GameServerManager;
import com.l2kt.loginserver.network.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket
{
	public AuthResponse(int serverId)
	{
		writeC(0x02);
		writeC(serverId);
		writeS(GameServerManager.INSTANCE.getServerNames().get(serverId));
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}