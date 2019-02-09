package com.l2kt.gameserver.network.serverpackets;

import com.l2kt.Config;
import com.l2kt.commons.logging.CLogger;
import com.l2kt.commons.mmocore.SendablePacket;

import com.l2kt.gameserver.network.L2GameClient;

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected static final CLogger LOGGER = new CLogger(L2GameServerPacket.class.getName());
	
	protected abstract void writeImpl();
	
	@Override
	protected void write()
	{
		if (Config.PACKET_HANDLER_DEBUG)
			LOGGER.info(getType());
		
		try
		{
			writeImpl();
		}
		catch (Throwable t)
		{
			LOGGER.error("Failed writing {} for {}. ", t, getType(), getClient().toString());
		}
	}
	
	public void runImpl()
	{
	}
	
	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}
}