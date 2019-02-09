package com.l2kt.loginserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.loginserver.LoginController;
import com.l2kt.loginserver.network.SessionKey;
import com.l2kt.loginserver.network.serverpackets.LoginFail;
import com.l2kt.loginserver.network.serverpackets.PlayFail;
import com.l2kt.loginserver.network.serverpackets.PlayOk;

public class RequestServerLogin extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;
	
	public int getSessionKey1()
	{
		return _skey1;
	}
	
	public int getSessionKey2()
	{
		return _skey2;
	}
	
	public int getServerID()
	{
		return _serverId;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 9)
		{
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		SessionKey sk = getClient().getSessionKey();
		
		// if we didnt showed the license we cant check these values
		if (!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			if (LoginController.getInstance().isLoginPossible(getClient(), _serverId))
			{
				getClient().setJoinedGS(true);
				getClient().sendPacket(new PlayOk(sk));
			}
			else
				getClient().close(PlayFail.REASON_TOO_MANY_PLAYERS);
		}
		else
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
	}
}
